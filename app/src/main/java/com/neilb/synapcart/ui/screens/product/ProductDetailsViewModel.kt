package com.neilb.synapcart.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.R
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.data.remote.toUiText
import com.neilb.synapcart.domain.use_case.favorites.FavoritesUseCases
import com.neilb.synapcart.util.SelectedProductStore
import com.neilb.synapcart.util.SessionManager
import com.neilb.synapcart.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    selectedProductStore: SelectedProductStore,
    sessionManager: SessionManager,
    private val favoritesUseCases: FavoritesUseCases,
    private val snackbarController: SnackbarController
) : ViewModel() {

    /** Null if the app was restarted on this screen; the screen then closes itself. */
    val product: ProductDTO? = selectedProductStore.product

    /** This product first, then up to three of the results it was shown with. */
    val compareCandidates: List<ProductDTO> = product?.let { p ->
        (listOf(p) + selectedProductStore.siblings.filter { it != p && it.link != p.link }).take(4)
    }.orEmpty()

    val canCompare: Boolean get() = compareCandidates.size >= 2

    val isFavorite: StateFlow<Boolean> = favoritesUseCases.favoriteLinks
        .map { links -> product?.link?.let { it in links } ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currency: StateFlow<String> = sessionManager.currency
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionManager.DEFAULT_CURRENCY)

    val isPremium: StateFlow<Boolean> = sessionManager.isPremium
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun toggleFavorite() {
        val p = product ?: return
        viewModelScope.launch {
            favoritesUseCases.toggle(p).fold(
                onSuccess = { added ->
                    snackbarController.showSnackbar(if (added) R.string.favorite_added else R.string.favorite_removed)
                },
                onFailure = { e -> snackbarController.showSnackbar(e.toUiText(R.string.error_favorite_update)) }
            )
        }
    }

    /** Price alerts need the premium backend (roadmap Phase 2/5). */
    fun onPriceAlert() {
        viewModelScope.launch { snackbarController.showSnackbar(R.string.price_alert_coming_soon) }
    }
}
