package com.neilb.synapcart.ui.screens.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.R
import com.neilb.synapcart.data.model.CompareItem
import com.neilb.synapcart.data.model.CompareResponse
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.data.remote.ApiException
import com.neilb.synapcart.data.remote.toUiText
import com.neilb.synapcart.domain.use_case.chat.ChatUseCases
import com.neilb.synapcart.util.SelectedProductStore
import com.neilb.synapcart.util.SessionManager
import com.neilb.synapcart.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The AI part of the comparison (`POST /chat/compare`). */
sealed interface AiCompareState {
    data object Loading : AiCompareState
    data class Ready(val response: CompareResponse) : AiCompareState {
        fun itemFor(index: Int): CompareItem? = response.items?.getOrNull(index)
    }
    data class Failed(val message: UiText) : AiCompareState
}

/** Side-by-side comparison of 2–4 products plus the AI pros/cons and verdict (premium). */
@HiltViewModel
class CompareViewModel @Inject constructor(
    selectedProductStore: SelectedProductStore,
    sessionManager: SessionManager,
    private val chatUseCases: ChatUseCases
) : ViewModel() {

    /** Empty after process death; the screen then closes. */
    val products: List<ProductDTO> = selectedProductStore.compareProducts

    /** Index of the cheapest product with a known price, or null. */
    val cheapestIndex: Int? = products.withIndex()
        .filter { it.value.price != null }
        .minByOrNull { it.value.price!! }
        ?.index
        ?.takeIf { products.count { p -> p.price != null } >= 2 }

    val currency: StateFlow<String> = sessionManager.currency
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionManager.DEFAULT_CURRENCY)

    private val _ai = MutableStateFlow<AiCompareState>(AiCompareState.Loading)
    val ai: StateFlow<AiCompareState> = _ai.asStateFlow()

    /** The server says this is premium-only (plan expired since the screen was opened). */
    private val _paywallEvents = Channel<Unit>(Channel.CONFLATED)
    val paywallEvents = _paywallEvents.receiveAsFlow()

    init {
        if (products.size >= 2) loadAi()
    }

    fun loadAi() {
        _ai.value = AiCompareState.Loading
        viewModelScope.launch {
            chatUseCases.compareProducts(products).fold(
                onSuccess = { _ai.value = AiCompareState.Ready(it) },
                onFailure = { e ->
                    if (e is ApiException.QuotaExceeded) _paywallEvents.send(Unit)
                    _ai.value = AiCompareState.Failed(e.toUiText(R.string.error_compare))
                }
            )
        }
    }
}
