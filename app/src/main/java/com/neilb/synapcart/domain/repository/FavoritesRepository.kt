package com.neilb.synapcart.domain.repository

import com.neilb.synapcart.data.model.AddFavoriteRequest
import com.neilb.synapcart.data.model.FavoriteResponse

interface FavoritesRepository {
    suspend fun getFavorites(): Result<List<FavoriteResponse>>
    suspend fun addFavorite(request: AddFavoriteRequest): Result<FavoriteResponse>
    suspend fun removeFavorite(favoriteId: Int): Result<Unit>
}