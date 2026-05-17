package com.neilb.synapcart.data.repository

import com.neilb.synapcart.data.model.AddFavoriteRequest
import com.neilb.synapcart.data.model.FavoriteResponse
import com.neilb.synapcart.data.remote.FavoritesApiService
import com.neilb.synapcart.domain.repository.FavoritesRepository

class FavoritesRepositoryImpl(
    private val apiService: FavoritesApiService
) : FavoritesRepository {

    override suspend fun getFavorites(): Result<List<FavoriteResponse>> {
        return try {
            val response = apiService.getFavorites()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addFavorite(request: AddFavoriteRequest): Result<FavoriteResponse> {
        return try {
            val response = apiService.addFavorite(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFavorite(favoriteId: Int): Result<Unit> {
        return try {
            apiService.removeFavorite(favoriteId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}