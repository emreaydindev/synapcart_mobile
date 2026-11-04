package com.neilb.synapcart.data.repository

import com.neilb.synapcart.data.billing.UserPlanRepository
import com.neilb.synapcart.data.model.ForgotPasswordRequest
import com.neilb.synapcart.data.push.PushRepository
import com.neilb.synapcart.data.model.LoginRequest
import com.neilb.synapcart.data.model.RegisterRequest
import com.neilb.synapcart.data.model.ResetPasswordRequest
import com.neilb.synapcart.data.remote.AuthApiService
import com.neilb.synapcart.data.remote.safeApiCall
import com.neilb.synapcart.domain.repository.AuthRepository
import com.neilb.synapcart.domain.repository.FavoritesRepository
import com.neilb.synapcart.util.SessionManager

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager,
    private val favoritesRepository: FavoritesRepository,
    private val planRepository: UserPlanRepository,
    private val pushRepository: PushRepository
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> = safeApiCall {
        val response = apiService.login(LoginRequest(email.trim(), password))
        favoritesRepository.clear()
        planRepository.clear()
        sessionManager.saveAuthToken(response.accessToken, response.userName)
    }

    override suspend fun register(email: String, password: String, fullName: String): Result<Unit> {
        val created = safeApiCall {
            apiService.register(RegisterRequest(email.trim(), password, fullName.trim()))
        }
        if (created.isFailure) return Result.failure(created.exceptionOrNull()!!)
        return login(email, password)
    }

    override suspend fun forgotPassword(email: String): Result<Unit> = safeApiCall {
        apiService.forgotPassword(ForgotPasswordRequest(email.trim()))
        Unit
    }

    override suspend fun resetPassword(token: String, newPassword: String): Result<Unit> = safeApiCall {
        apiService.resetPassword(ResetPasswordRequest(token, newPassword))
        Unit
    }

    override suspend fun logout() {
        // While the token is still valid, so this device stops getting the user's alerts.
        pushRepository.unregister()
        favoritesRepository.clear()
        planRepository.clear()
        sessionManager.clearSession()
    }
}
