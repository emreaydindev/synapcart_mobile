package com.neilb.synapcart.data.repository

import com.neilb.synapcart.data.model.ForgotPasswordRequest
import com.neilb.synapcart.data.model.LoginRequest
import com.neilb.synapcart.data.model.RegisterRequest
import com.neilb.synapcart.data.model.ResetPasswordRequest
import com.neilb.synapcart.data.remote.AuthApiService
import com.neilb.synapcart.domain.repository.AuthRepository
import com.neilb.synapcart.util.SessionManager

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            sessionManager.saveAuthToken(response.accessToken, response.userName ?: "")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, fullName: String): Result<Unit> {
        return try {
            val response = apiService.register(RegisterRequest(email, password, fullName))
            sessionManager.saveAuthToken(response.accessToken, response.userName ?: fullName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            apiService.forgotPassword(ForgotPasswordRequest(email))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(token: String, newPassword: String): Result<Unit> {
        return try {
            apiService.resetPassword(ResetPasswordRequest(token, newPassword))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }
}