package com.neilb.synapcart.data.remote

import com.neilb.synapcart.data.model.AccountDeleteRequest
import com.neilb.synapcart.data.model.AuthResponse
import com.neilb.synapcart.data.model.DeviceTokenRequest
import com.neilb.synapcart.data.model.PasswordChangeRequest
import com.neilb.synapcart.data.model.UserProfile
import com.neilb.synapcart.data.model.UserProfileUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UserApiService {

    @GET("api/v1/user/me")
    suspend fun getUserProfile(): UserProfile

    @PATCH("api/v1/user/me")
    suspend fun updateProfile(@Body request: UserProfileUpdateRequest): UserProfile

    /** Returns a fresh token; all other tokens are revoked. */
    @POST("api/v1/user/me/password")
    suspend fun changePassword(@Body request: PasswordChangeRequest): AuthResponse

    @HTTP(method = "DELETE", path = "api/v1/user/me", hasBody = true)
    suspend fun deleteAccount(@Body request: AccountDeleteRequest): Response<Unit>

    /** Registers this device's FCM token for price alerts (idempotent). */
    @POST("api/v1/user/me/device-token")
    suspend fun registerDeviceToken(@Body request: DeviceTokenRequest): Response<Unit>

    @HTTP(method = "DELETE", path = "api/v1/user/me/device-token", hasBody = true)
    suspend fun unregisterDeviceToken(@Body request: DeviceTokenRequest): Response<Unit>
}
