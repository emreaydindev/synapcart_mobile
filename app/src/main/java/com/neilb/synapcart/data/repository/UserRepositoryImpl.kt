package com.neilb.synapcart.data.repository

import com.neilb.synapcart.data.model.AccountDeleteRequest
import com.neilb.synapcart.data.model.PasswordChangeRequest
import com.neilb.synapcart.data.model.UserProfile
import com.neilb.synapcart.data.model.UserProfileUpdateRequest
import com.neilb.synapcart.data.remote.UserApiService
import com.neilb.synapcart.data.remote.requireSuccess
import com.neilb.synapcart.data.remote.safeApiCall
import com.neilb.synapcart.domain.repository.FavoritesRepository
import com.neilb.synapcart.domain.repository.UserRepository
import com.neilb.synapcart.util.SessionManager

class UserRepositoryImpl(
    private val apiService: UserApiService,
    private val sessionManager: SessionManager,
    private val favoritesRepository: FavoritesRepository
) : UserRepository {

    override suspend fun getUserProfile(): Result<UserProfile> =
        safeApiCall { apiService.getUserProfile() }.onSuccess { cache(it) }

    override suspend fun updateProfile(fullName: String?, language: String?, currency: String?): Result<UserProfile> =
        safeApiCall {
            apiService.updateProfile(UserProfileUpdateRequest(fullName?.trim(), language, currency))
        }.onSuccess { cache(it) }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = safeApiCall {
        val token = apiService.changePassword(PasswordChangeRequest(currentPassword, newPassword))
        // Every other token was revoked; keep this device signed in with the new one.
        sessionManager.saveAuthToken(token.accessToken, token.userName)
    }

    override suspend fun deleteAccount(password: String?): Result<Unit> = safeApiCall {
        apiService.deleteAccount(AccountDeleteRequest(password?.takeIf { it.isNotEmpty() })).requireSuccess()
        favoritesRepository.clear()
        sessionManager.clearSession()
        sessionManager.saveOnboardingCompleted(false)
    }

    private suspend fun cache(profile: UserProfile) {
        profile.fullName?.takeIf { it.isNotBlank() }?.let { sessionManager.saveUserName(it) }
        sessionManager.savePreferences(profile.language, profile.currency)
    }
}
