package com.neilb.synapcart.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.data.model.FavoriteResponse
import com.neilb.synapcart.domain.use_case.favorites.FavoritesUseCases
import com.neilb.synapcart.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesUseCases: FavoritesUseCases,
    private val snackbarController: SnackbarController
) : ViewModel() {

    private val _favorites = MutableStateFlow<List<FavoriteResponse>>(emptyList())
    val favorites: StateFlow<List<FavoriteResponse>> = _favorites.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchFavorites()
    }

    private fun fetchFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = favoritesUseCases.getFavorites()

            result.fold(
                onSuccess = { data ->
                    _favorites.value = data
                },
                onFailure = { exception ->
                    snackbarController.showSnackbar("Favoriler yüklenemedi: ${exception.message}")
                }
            )
            _isLoading.value = false
        }
    }

    fun removeFavorite(favoriteId: Int) {
        viewModelScope.launch {
            val currentList = _favorites.value
            _favorites.update { it.filter { item -> item.id != favoriteId } }

            val result = favoritesUseCases.removeFavorite(favoriteId)

            result.fold(
                onSuccess = {
                    snackbarController.showSnackbar("Ürün favorilerden çıkarıldı.")
                },
                onFailure = {
                    _favorites.value = currentList
                    snackbarController.showSnackbar("Silme işlemi başarısız oldu.")
                }
            )
        }
    }
}