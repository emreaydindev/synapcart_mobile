package com.neilb.synapcart.data.repository

import com.neilb.synapcart.data.billing.UserPlanRepository
import com.neilb.synapcart.data.model.ForgotPasswordRequest
import com.neilb.synapcart.data.model.LoginRequest
import com.neilb.synapcart.data.model.RegisterRequest
import com.neilb.synapcart.data.model.ResetPasswordRequest
import com.neilb.synapcart.data.remote.SynapCartApi
import com.neilb.synapcart.data.remote.safeApiCall
import com.neilb.synapcart.domain.repository.AuthRepository
import com.neilb.synapcart.domain.repository.FavoritesRepository
import com.neilb.synapcart.util.SessionManager

/**
 * Platform callbacks the shared repositories can't implement themselves: push token
 * cleanup (FCM SDK on both platforms) and applying the profile language to the UI.
 */
interface PlatformHooks {
    /** Runs while the token is still valid, before the session is cleared (logout). */
    suspend fun beforeSignOut() {}

    /** Called with every fresh profile from the backend (login, refresh, purchase). */
    suspend fun onProfileLoaded(language: String?) {}
}

object NoPlatformHooks : PlatformHooks

class AuthRepositoryImpl(
    private val apiService: SynapCartApi,
    private val sessionManager: SessionManager,
    private val favoritesRepository: FavoritesRepository,
    private val planRepository: UserPlanRepository,
    private val hooks: PlatformHooks = NoPlatformHooks
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
        runCatching { hooks.beforeSignOut() }
        favoritesRepository.clear()
        planRepository.clear()
        sessionManager.clearSession()
    }
}
