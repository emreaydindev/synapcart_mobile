package com.neilb.synapcart.data.remote

import com.neilb.synapcart.data.model.AddFavoriteRequest
import com.neilb.synapcart.data.model.FavoriteResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FavoritesApiService {

    @GET("api/v1/favorites/")
    suspend fun getFavorites(): List<FavoriteResponse>

    /** Idempotent: 201 for a new favorite, 200 with the existing row for a duplicate link. */
    @POST("api/v1/favorites/")
    suspend fun addFavorite(@Body request: AddFavoriteRequest): FavoriteResponse

    @DELETE("api/v1/favorites/{id}")
    suspend fun removeFavorite(@Path("id") favoriteId: Int): Response<Unit>

    /** Sets or clears (`target_price: null`) the price alert. Premium only (402 otherwise). */
    @PATCH("api/v1/favorites/{id}")
    suspend fun updateFavorite(@Path("id") favoriteId: Int, @Body body: RequestBody): FavoriteResponse

    @DELETE("api/v1/favorites/by-link")
    suspend fun removeFavoriteByLink(@Query("link") link: String): Response<Unit>
}
