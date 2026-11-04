package com.neilb.synapcart.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.R
import com.neilb.synapcart.data.billing.BillingRepository
import com.neilb.synapcart.data.model.AgentStreamEvent
import com.neilb.synapcart.data.push.PushRepository
import com.neilb.synapcart.util.NotificationPermissionController
import com.neilb.synapcart.data.remote.ApiException
import com.neilb.synapcart.data.remote.toUiText
import com.neilb.synapcart.util.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.data.model.SessionResponse
import com.neilb.synapcart.data.repository.ChatStreamRepository
import com.neilb.synapcart.domain.model.ChatMessage
import com.neilb.synapcart.domain.repository.AuthRepository
import com.neilb.synapcart.domain.use_case.chat.ChatUseCases
import com.neilb.synapcart.domain.use_case.favorites.FavoritesUseCases
import com.neilb.synapcart.domain.use_case.user.UserUseCases
import com.neilb.synapcart.util.SessionManager
import com.neilb.synapcart.util.SnackbarController
import com.neilb.synapcart.util.parseServerTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatSessionItem(
    val id: Int,
    /** Null until the server names the chat; the UI shows a placeholder. */
    val title: String?,
    val createdAt: Long?,
    /** Free plan: past the 10 newest chats; opening it shows the paywall. */
    val locked: Boolean = false
)

/** Free-plan usage from `/user/me` (`usage_today` / `daily_limit`). */
data class QuotaState(val used: Int, val limit: Int)

/** What the agent is doing for the in-flight message. */
enum class AgentPhase { SENDING, ROUTING, SEARCHING, THINKING }

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val chatUseCases: ChatUseCases,
    private val favoritesUseCases: FavoritesUseCases,
    private val snackbarController: SnackbarController,
    private val chatStreamRepository: ChatStreamRepository,
    private val userUseCases: UserUseCases,
    private val authRepository: AuthRepository,
    private val billingRepository: BillingRepository,
    private val pushRepository: PushRepository,
    private val notificationPermission: NotificationPermissionController
) : ViewModel() {

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _sessions = MutableStateFlow<List<ChatSessionItem>>(emptyList())
    val sessions: StateFlow<List<ChatSessionItem>> = _sessions.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    /** Non-null while a message is being answered. */
    private val _agentPhase = MutableStateFlow<AgentPhase?>(null)
    val agentPhase: StateFlow<AgentPhase?> = _agentPhase.asStateFlow()

    /** Products found for the in-flight answer, shown before the analysis arrives. */
    private val _pendingProducts = MutableStateFlow<List<ProductDTO>>(emptyList())
    val pendingProducts: StateFlow<List<ProductDTO>> = _pendingProducts.asStateFlow()

    private val _isLoadingMessages = MutableStateFlow(false)
    val isLoadingMessages: StateFlow<Boolean> = _isLoadingMessages.asStateFlow()

    private val _currentChatTitle = MutableStateFlow<String?>(null)
    val currentChatTitle: StateFlow<String?> = _currentChatTitle.asStateFlow()

    private val _currentSessionId = MutableStateFlow<Int?>(null)
    val currentSessionId: StateFlow<Int?> = _currentSessionId.asStateFlow()

    val favoriteLinks: StateFlow<Set<String>> = favoritesUseCases.favoriteLinks

    val currency: StateFlow<String> = sessionManager.currency
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionManager.DEFAULT_CURRENCY)

    val isProcessing: Boolean get() = _agentPhase.value != null

    val isPremium: StateFlow<Boolean> = sessionManager.isPremium
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _quota = MutableStateFlow<QuotaState?>(null)
    val quota: StateFlow<QuotaState?> = _quota.asStateFlow()

    /** Fires when the server refuses a message for quota reasons (HTTP 402). */
    private val _paywallEvents = Channel<Unit>(Channel.CONFLATED)
    val paywallEvents = _paywallEvents.receiveAsFlow()

    private var streamJob: Job? = null
    private var loadJob: Job? = null

    init {
        loadUserData()
        fetchChatHistory()
        viewModelScope.launch { favoritesUseCases.refresh() }
        // Once per process: re-verify Play subscriptions and register the FCM token.
        viewModelScope.launch {
            billingRepository.restorePurchases()
            pushRepository.syncToken()
        }
        // Plan changed (purchase, expiry): quota banner and locked chats change with it.
        viewModelScope.launch {
            isPremium.drop(1).collect {
                loadUserData()
                fetchChatHistory()
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _userName.value = sessionManager.userName.firstOrNull().orEmpty()
            // Also refreshes the cached name / language / currency. A 401 here is handled
            // globally (session cleared, user sent to login).
            userUseCases.getUser().onSuccess { profile ->
                profile.fullName?.takeIf { it.isNotBlank() }?.let { _userName.value = it }
                val used = profile.usageToday
                val limit = profile.dailyLimit
                _quota.value = if (!profile.isPremium && used != null && limit != null) QuotaState(used, limit) else null
            }
        }
    }

    fun fetchChatHistory() {
        viewModelScope.launch {
            chatUseCases.getSessions().fold(
                onSuccess = { list -> _sessions.value = list.map { it.toItem() } },
                onFailure = { e -> snackbarController.showSnackbar(e.toUiText(R.string.error_history_load)) }
            )
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun loadSession(session: ChatSessionItem) {
        if (session.locked) {
            _paywallEvents.trySend(Unit)
            return
        }
        if (session.id == _currentSessionId.value) return
        cancelInFlight()
        _currentSessionId.value = session.id
        _currentChatTitle.value = session.title
        _messages.value = emptyList()

        loadJob = viewModelScope.launch {
            _isLoadingMessages.value = true
            chatUseCases.getMessages(session.id).fold(
                onSuccess = { _messages.value = it },
                onFailure = { e -> onSendFailure(e, R.string.error_messages_load) }
            )
            _isLoadingMessages.value = false
        }
    }

    fun sendSuggestion(text: String) {
        _inputText.value = text
        handleSendMessage()
    }

    fun handleSendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty() || isProcessing) return

        _inputText.value = ""
        _messages.update { it + ChatMessage(text = text, isUser = true) }
        _pendingProducts.value = emptyList()
        _agentPhase.value = AgentPhase.SENDING
        if (_currentChatTitle.value == null) _currentChatTitle.value = text

        streamJob = viewModelScope.launch {
            val sessionId = _currentSessionId.value ?: chatUseCases.createSession().fold(
                onSuccess = { session ->
                    _currentSessionId.value = session.id
                    session.id
                },
                onFailure = { e ->
                    _agentPhase.value = null
                    onSendFailure(e, R.string.error_chat_start)
                    return@launch
                }
            )
            streamAnswer(sessionId, text)
        }
    }

    private suspend fun streamAnswer(sessionId: Int, text: String) {
        chatStreamRepository.streamAgent(sessionId, text)
            .catch { e -> onSendFailure(e, R.string.error_generic) }
            .onCompletion {
                _agentPhase.value = null
                _pendingProducts.value = emptyList()
            }
            .collect { event -> onStreamEvent(event) }
        fetchChatHistory() // the server sets the session title from the first message
    }

    private suspend fun onStreamEvent(event: AgentStreamEvent) {
        event.products?.let { _pendingProducts.value = it }
        when (event.status) {
            AgentStreamEvent.ROUTING -> _agentPhase.value = AgentPhase.ROUTING
            AgentStreamEvent.SEARCHING -> _agentPhase.value = AgentPhase.SEARCHING
            AgentStreamEvent.THINKING -> _agentPhase.value = AgentPhase.THINKING
            AgentStreamEvent.COMPLETED, AgentStreamEvent.OUT_OF_SCOPE -> {
                val answer = event.analysis.orEmpty()
                val products = event.products ?: _pendingProducts.value
                if (answer.isNotBlank() || products.isNotEmpty()) {
                    _messages.update { it + ChatMessage(text = answer, isUser = false, products = products) }
                }
                _quota.update { q -> q?.copy(used = q.used + 1) }
            }
            AgentStreamEvent.ERROR -> {
                snackbarController.showSnackbar(
                    event.analysis?.let { UiText.Raw(it) } ?: UiText.Res(R.string.error_try_again)
                )
            }
        }
    }

    fun toggleFavorite(product: ProductDTO) {
        viewModelScope.launch {
            favoritesUseCases.toggle(product).fold(
                onSuccess = { added ->
                    snackbarController.showSnackbar(if (added) R.string.favorite_added else R.string.favorite_removed)
                    if (added) notificationPermission.maybeRequest()
                },
                onFailure = { e -> snackbarController.showSnackbar(e.toUiText(R.string.error_favorite_update)) }
            )
        }
    }

    fun renameSession(sessionId: Int, title: String) {
        val newTitle = title.trim()
        if (newTitle.isEmpty()) return
        viewModelScope.launch {
            chatUseCases.renameSession(sessionId, newTitle).fold(
                onSuccess = { updated ->
                    _sessions.update { list -> list.map { if (it.id == sessionId) updated.toItem() else it } }
                    if (_currentSessionId.value == sessionId) _currentChatTitle.value = updated.title
                },
                onFailure = { e -> snackbarController.showSnackbar(e.toUiText(R.string.error_chat_rename)) }
            )
        }
    }

    fun deleteSession(sessionId: Int) {
        viewModelScope.launch {
            chatUseCases.deleteSession(sessionId).fold(
                onSuccess = {
                    _sessions.update { list -> list.filterNot { it.id == sessionId } }
                    if (_currentSessionId.value == sessionId) resetChat()
                },
                onFailure = { e -> snackbarController.showSnackbar(e.toUiText(R.string.error_chat_delete)) }
            )
        }
    }

    private suspend fun onSendFailure(e: Throwable, fallback: Int) {
        if (e is ApiException.QuotaExceeded) {
            if (e.code == ApiException.CODE_QUOTA) _quota.update { q -> q?.copy(used = q.limit) }
            // Premium users only hit the soft cap; the paywall has nothing more to sell them.
            if (!isPremium.value) _paywallEvents.send(Unit)
        }
        snackbarController.showSnackbar(e.toUiText(fallback))
    }

    fun resetChat() {
        cancelInFlight()
        _messages.value = emptyList()
        _currentChatTitle.value = null
        _currentSessionId.value = null
        _inputText.value = ""
    }

    private fun cancelInFlight() {
        streamJob?.cancel()
        loadJob?.cancel()
        _agentPhase.value = null
        _pendingProducts.value = emptyList()
        _isLoadingMessages.value = false
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            cancelInFlight()
            authRepository.logout()
            onLogoutComplete()
        }
    }

    private fun SessionResponse.toItem() = ChatSessionItem(
        id = id,
        title = title?.takeIf { it.isNotBlank() },
        createdAt = parseServerTimestamp(createdAt),
        locked = locked == true
    )
}
