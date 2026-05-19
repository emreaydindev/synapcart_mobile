package com.neilb.synapcart.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.data.model.AddFavoriteRequest
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.data.repository.ChatStreamRepository
import com.neilb.synapcart.domain.model.ChatMessage
import com.neilb.synapcart.domain.use_case.chat.ChatUseCases
import com.neilb.synapcart.domain.use_case.favorites.FavoritesUseCases
import com.neilb.synapcart.domain.use_case.user.UserUseCases
import com.neilb.synapcart.util.SessionManager
import com.neilb.synapcart.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val chatUseCases: ChatUseCases,
    private val favoritesUseCases: FavoritesUseCases,
    private val snackbarController: SnackbarController,
    private val chatStreamRepository: ChatStreamRepository,
    private val userUseCases: UserUseCases
) : ViewModel() {

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    val chatHistory: StateFlow<List<Pair<Int, String>>> = _chatHistory.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _statusText = MutableStateFlow<String?>(null)
    val statusText: StateFlow<String?> = _statusText.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _products = MutableStateFlow<List<ProductDTO>>(emptyList())
    val products: StateFlow<List<ProductDTO>> = _products.asStateFlow()

    private val _currentChatTitle = MutableStateFlow<String?>(null)
    val currentChatTitle: StateFlow<String?> = _currentChatTitle.asStateFlow()

    private val _currentSessionId = MutableStateFlow<Int?>(null)
    val currentSessionId: StateFlow<Int?> = _currentSessionId.asStateFlow()

    init {
        loadUserDataAndSync()
        fetchChatHistory()
    }

    private fun loadUserDataAndSync() {
        viewModelScope.launch {
            val localName = sessionManager.userName.firstOrNull()
            _userName.value = localName ?: ""

            val result = userUseCases.getUser()
            result.fold(
                onSuccess = { userProfile ->
                    if (userProfile.fullName != localName) {
                        sessionManager.saveUserName(userProfile.fullName)
                        _userName.value = userProfile.fullName
                    }
                },
                onFailure = { e ->
                    if (e is retrofit2.HttpException && e.code() == 401) {
                        sessionManager.clearSession()
                        snackbarController.showSnackbar("Oturum süresi doldu.")
                    }
                }
            )
        }
    }

    fun fetchChatHistory() {
        viewModelScope.launch {
            val result = chatUseCases.getSessions()
            result.fold(
                onSuccess = { sessions ->
                    _chatHistory.value = sessions.map { session ->
                        Pair(session.id, session.title ?: "Sohbet #${session.id}")
                    }
                },
                onFailure = { e ->
                    e.printStackTrace()
                    snackbarController.showSnackbar("Geçmiş yüklenirken hata oluştu: ${e.message}")
                }
            )
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun loadSession(sessionId: Int, title: String) {
        _currentSessionId.value = sessionId
        _currentChatTitle.value = title
        _messages.value = emptyList()
        _products.value = emptyList()
        _statusText.value = null

        viewModelScope.launch {
            val result = chatUseCases.getMessages(sessionId)
            result.fold(
                onSuccess = { messagesList ->
                    _messages.value = messagesList
                    snackbarController.showSnackbar("$title yüklendi.")
                },
                onFailure = { e ->
                    e.printStackTrace()
                    snackbarController.showSnackbar("Mesajlar yüklenemedi: ${e.message}")
                }
            )
        }
    }

    fun handleSendMessage() {
        val currentText = _inputText.value.trim()
        if (currentText.isEmpty() || _isProcessing.value) return

        viewModelScope.launch {
            val sessionId = _currentSessionId.value
            if (sessionId == null) {
                _isProcessing.value = true
                _statusText.value = "Oturum oluşturuluyor..."
                val result = chatUseCases.createSession()
                result.fold(
                    onSuccess = { session ->
                        _currentSessionId.value = session.id
                        fetchChatHistory()
                        executeChatStream(session.id, currentText)
                    },
                    onFailure = { e ->
                        _isProcessing.value = false
                        _statusText.value = null
                        handleSessionError(e)
                    }
                )
            } else {
                executeChatStream(sessionId, currentText)
            }
        }
    }

    private var answer: String? = null

    private fun executeChatStream(sessionId: Int, currentText: String) {
        if (_currentChatTitle.value == null) {
            _currentChatTitle.value = currentText
        }

        val userMessage = ChatMessage(text = currentText, isUser = true)
        _messages.update { it + userMessage }
        _inputText.value = ""
        _products.value = emptyList()

        _isProcessing.value = true
        _statusText.value = "Gönderildi"

        viewModelScope.launch {
            chatStreamRepository.streamAgent(sessionId, currentText)
                .onStart { _statusText.value = "İşleniyor" }
                .catch { e ->
                    _isProcessing.value = false
                    _statusText.value = null
                    snackbarController.showSnackbar(e.message ?: "İletişim hatası")
                }
                .onCompletion {
                    _isProcessing.value = false
                    _statusText.value = null
                }
                .collect { event ->
                    if (event.status == "error") {
                        event.analysis?.let { snackbarController.showSnackbar(it) }
                        _isProcessing.value = false
                        return@collect
                    }

                    event.status?.let { s ->
                        _statusText.value = when (s) {
                            "searching" -> "İnternette aranıyor..."
                            "thinking" -> "Düşünüyor..."
                            "out_of_scope" -> "Kapsam dışı"
                            "completed" -> null
                            else -> s
                        }
                    }

                    event.analysis?.let { analysisText ->
                        answer = analysisText
                    }

                    event.products?.let { list ->
                        _products.value = list
                    }

                    if (event.status == "completed") {
                        if (answer?.isNotEmpty() == true) {
                            val botMsg = ChatMessage(text = answer.toString(), isUser = false)
                            _messages.update { it + botMsg }
                            answer = null
                        }
                        _isProcessing.value = false
                        delay(1000)
                        _statusText.value = null
                        fetchChatHistory()
                    }
                }
        }
    }

    private fun handleSessionError(e: Throwable) {
        val isUnauthorized = e is retrofit2.HttpException && e.code() == 401
        viewModelScope.launch {
            if (isUnauthorized) {
                sessionManager.clearSession()
                snackbarController.showSnackbar("Oturum süresi doldu. Tekrar giriş yapın.")
            } else {
                snackbarController.showSnackbar(e.message ?: "Oturum oluşturulamadı")
            }
        }

    }

    fun toggleFavorite(product: ProductDTO) {
        viewModelScope.launch {
            val request = AddFavoriteRequest(
                productTitle = product.title ?: "Bilinmeyen Ürün",
                productLink = product.link ?: "",
                price = product.price?.toString() ?: "0",
                source = product.source ?: "Bilinmiyor",
                thumbnailUrl = product.thumbnail
            )
            val result = favoritesUseCases.addFavorite(request)
            result.fold(
                onSuccess = { snackbarController.showSnackbar("Favorilere eklendi!") },
                onFailure = { e ->
                    e.printStackTrace()
                    snackbarController.showSnackbar("Favoriye eklenemedi.")
                }
            )
        }
    }

    fun resetChat() {
        _messages.value = emptyList()
        _products.value = emptyList()
        _currentChatTitle.value = null
        _currentSessionId.value = null
        _statusText.value = null
        _inputText.value = ""
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            sessionManager.clearSession()
            onLogoutComplete()
        }
    }
}