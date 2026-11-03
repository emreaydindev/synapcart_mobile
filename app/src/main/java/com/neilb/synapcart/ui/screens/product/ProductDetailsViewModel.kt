package com.neilb.synapcart.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.data.model.ProductDTO
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

    val isFavorite: StateFlow<Boolean> = favoritesUseCases.favoriteLinks
        .map { links -> product?.link?.let { it in links } ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currency: StateFlow<String> = sessionManager.currency
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionManager.DEFAULT_CURRENCY)

    fun toggleFavorite() {
        val p = product ?: return
        viewModelScope.launch {
            favoritesUseCases.toggle(p).fold(
                onSuccess = { added ->
                    snackbarController.showSnackbar(if (added) "Favorilere eklendi." else "Favorilerden çıkarıldı.")
                },
                onFailure = { e -> snackbarController.showSnackbar(e.message ?: "Favori güncellenemedi.") }
            )
        }
    }
}
