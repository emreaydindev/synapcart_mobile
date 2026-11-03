package com.neilb.synapcart.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.data.model.FavoriteResponse
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.domain.use_case.favorites.FavoritesUseCases
import com.neilb.synapcart.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesUseCases: FavoritesUseCases,
    private val snackbarController: SnackbarController
) : ViewModel() {

    /** Shared cache, so removals here are reflected on every other screen. */
    val favorites: StateFlow<List<FavoriteResponse>> = favoritesUseCases.favorites

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            favoritesUseCases.refresh().fold(
                onSuccess = { _loadError.value = null },
                onFailure = { e -> _loadError.value = e.message ?: "Favoriler yüklenemedi." }
            )
            _isLoading.value = false
        }
    }

    fun removeFavorite(favorite: FavoriteResponse) {
        viewModelScope.launch {
            favoritesUseCases.remove(favorite).fold(
                onSuccess = { snackbarController.showSnackbar("Ürün favorilerden çıkarıldı.") },
                onFailure = { e -> snackbarController.showSnackbar(e.message ?: "Silme işlemi başarısız oldu.") }
            )
        }
    }

    companion object {
        fun FavoriteResponse.toProduct() = ProductDTO(
            title = productTitle,
            price = price,
            source = source,
            link = productLink,
            thumbnail = thumbnailUrl
        )
    }
}
