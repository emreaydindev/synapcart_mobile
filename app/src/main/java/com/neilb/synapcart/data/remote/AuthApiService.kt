package com.neilb.synapcart.data.remote

import com.neilb.synapcart.data.model.AuthResponse
import com.neilb.synapcart.data.model.ForgotPasswordRequest
import com.neilb.synapcart.data.model.LoginRequest
import com.neilb.synapcart.data.model.MessageResponseBody
import com.neilb.synapcart.data.model.RegisterRequest
import com.neilb.synapcart.data.model.ResetPasswordRequest
import com.neilb.synapcart.data.model.UserProfile
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    /** Returns the created user (201), not a token; log in afterwards. */
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): UserProfile

    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): MessageResponseBody

    @POST("api/v1/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): MessageResponseBody
}
