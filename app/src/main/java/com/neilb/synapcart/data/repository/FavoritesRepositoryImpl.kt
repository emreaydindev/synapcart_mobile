package com.neilb.synapcart.data.repository

import com.google.gson.Gson
import com.neilb.synapcart.data.model.AddFavoriteRequest
import com.neilb.synapcart.data.model.ErrorResponse
import com.neilb.synapcart.data.model.FavoriteResponse
import com.neilb.synapcart.data.remote.FavoritesApiService
import com.neilb.synapcart.domain.repository.FavoritesRepository
import retrofit2.HttpException

class FavoritesRepositoryImpl(
    private val apiService: FavoritesApiService
) : FavoritesRepository {

    override suspend fun getFavorites(): Result<List<FavoriteResponse>> {
        return try {
            val response = apiService.getFavorites()
            Result.success(response)
        } catch (e: HttpException) {
            val errorJson = e.response()?.errorBody()?.string()
            val errorMessage = try {
                Gson().fromJson(errorJson, ErrorResponse::class.java).detail
            } catch (_: Exception) {
                "Sunucu ile iletişimde bir sorun oluştu."
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addFavorite(request: AddFavoriteRequest): Result<FavoriteResponse> {
        return try {
            val response = apiService.addFavorite(request)
            Result.success(response)
        } catch (e: HttpException) {
            val errorJson = e.response()?.errorBody()?.string()
            val errorMessage = try {
                Gson().fromJson(errorJson, ErrorResponse::class.java).detail
            } catch (_: Exception) {
                "Sunucu ile iletişimde bir sorun oluştu."
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFavorite(favoriteId: Int): Result<Unit> {
        return try {
            apiService.removeFavorite(favoriteId)
            Result.success(Unit)
        } catch (e: HttpException) {
            val errorJson = e.response()?.errorBody()?.string()
            val errorMessage = try {
                Gson().fromJson(errorJson, ErrorResponse::class.java).detail
            } catch (_: Exception) {
                "Sunucu ile iletişimde bir sorun oluştu."
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}