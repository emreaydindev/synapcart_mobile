package com.neilb.synapcart.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.data.model.AddFavoriteRequest
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.domain.use_case.favorites.FavoritesUseCases
import com.neilb.synapcart.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val favoritesUseCases: FavoritesUseCases,
    private val snackbarController: SnackbarController
) : ViewModel() {

    fun toggleFavorite(product: ProductDTO) {
        viewModelScope.launch {
            val request = AddFavoriteRequest(
                productTitle = product.title,
                productLink = product.link ?: "",
                price = product.price?.toString() ?: "0",
                source = product.source ?: "Bilinmiyor",
                thumbnailUrl = product.thumbnail
            )
            val result = favoritesUseCases.addFavorite(request)
            result.fold(
                onSuccess = { snackbarController.showSnackbar("Favorilere eklendi!") },
                onFailure = { snackbarController.showSnackbar("Favoriye eklenemedi.") }
            )
        }
    }
}