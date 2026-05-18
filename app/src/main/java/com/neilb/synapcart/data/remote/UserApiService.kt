package com.neilb.synapcart.data.remote

import com.neilb.synapcart.data.model.UserProfileUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH

interface UserApiService {

    @PATCH("api/v1/user/me")
    suspend fun updateProfile(@Body request: UserProfileUpdateRequest)

    @DELETE("api/v1/user/me")
    suspend fun deleteAccount()
}