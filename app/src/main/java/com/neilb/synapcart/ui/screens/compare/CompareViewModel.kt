package com.neilb.synapcart.ui.screens.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.util.SelectedProductStore
import com.neilb.synapcart.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Side-by-side comparison of 2–4 products. The AI verdict (pros/cons from `/chat/compare`)
 * arrives with the premium backend (roadmap Phase 2/5).
 */
@HiltViewModel
class CompareViewModel @Inject constructor(
    selectedProductStore: SelectedProductStore,
    sessionManager: SessionManager
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
}
