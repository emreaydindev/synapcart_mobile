package com.neilb.synapcart.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.R
import com.neilb.synapcart.data.model.FavoriteResponse
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.data.remote.ApiException
import com.neilb.synapcart.util.NotificationPermissionController
import com.neilb.synapcart.util.formatPrice
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
    private val snackbarController: SnackbarController,
    private val notificationPermission: NotificationPermissionController
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

    /** The saved favorite for this product (it carries the price alert), if any. */
    val favorite: StateFlow<FavoriteResponse?> = favoritesUseCases.favorites
        .map { list -> product?.link?.let { link -> list.firstOrNull { it.productLink == link } } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Fires when the server refuses a premium action (402). */
    private val _paywallEvents = Channel<Unit>(Channel.CONFLATED)
    val paywallEvents = _paywallEvents.receiveAsFlow()

    private val _savingAlert = MutableStateFlow(false)
    val savingAlert: StateFlow<Boolean> = _savingAlert.asStateFlow()

    fun toggleFavorite() {
        val p = product ?: return
        viewModelScope.launch {
            favoritesUseCases.toggle(p).fold(
                onSuccess = { added ->
                    snackbarController.showSnackbar(if (added) R.string.favorite_added else R.string.favorite_removed)
                    if (added) notificationPermission.maybeRequest()
                },
                onFailure = { e -> snackbarController.showSnackbar(e.toUiText(R.string.error_favorite_update)) }
            )
        }
    }

    /** Sets the alert to [targetPrice], or removes it when null. Favorites the product if needed. */
    fun setPriceAlert(targetPrice: Double?) {
        val p = product ?: return
        if (_savingAlert.value) return
        _savingAlert.value = true
        viewModelScope.launch {
            favoritesUseCases.setPriceAlert(p, targetPrice).fold(
                onSuccess = { saved ->
                    val target = saved.targetPrice
                    if (target != null) {
                        val currency = saved.currency ?: this@ProductDetailsViewModel.currency.value
                        snackbarController.showSnackbar(
                            R.string.price_alert_saved,
                            formatPrice(target, currency) ?: target.toString()
                        )
                        notificationPermission.maybeRequest(force = true)
                    } else {
                        snackbarController.showSnackbar(R.string.price_alert_removed)
                    }
                },
                onFailure = { e ->
                    if (e is ApiException.QuotaExceeded) _paywallEvents.send(Unit)
                    snackbarController.showSnackbar(e.toUiText(R.string.error_price_alert))
                }
            )
            _savingAlert.value = false
        }
    }
}
