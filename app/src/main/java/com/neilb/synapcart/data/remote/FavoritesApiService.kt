package com.neilb.synapcart.data.remote

import com.neilb.synapcart.data.model.AddFavoriteRequest
import com.neilb.synapcart.data.model.FavoriteResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoritesApiService {

    @GET("api/v1/favorites")
    suspend fun getFavorites(): List<FavoriteResponse>

    @POST("api/v1/favorites")
    suspend fun addFavorite(@Body request: AddFavoriteRequest): FavoriteResponse

    @DELETE("api/v1/favorites/{id}")
    suspend fun removeFavorite(@Path("id") favoriteId: Int)
}