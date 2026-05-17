package com.neilb.synapcart.domain.use_case.favorites

import com.neilb.synapcart.data.model.AddFavoriteRequest
import com.neilb.synapcart.domain.repository.FavoritesRepository

class GetFavoritesUseCase(private val repository: FavoritesRepository) {
    suspend operator fun invoke() = repository.getFavorites()
}

class AddFavoriteUseCase(private val repository: FavoritesRepository) {
    suspend operator fun invoke(request: AddFavoriteRequest) = repository.addFavorite(request)
}

class RemoveFavoriteUseCase(private val repository: FavoritesRepository) {
    suspend operator fun invoke(favoriteId: Int) = repository.removeFavorite(favoriteId)
}

data class FavoritesUseCases(
    val getFavorites: GetFavoritesUseCase,
    val addFavorite: AddFavoriteUseCase,
    val removeFavorite: RemoveFavoriteUseCase
)