package com.neilb.synapcart.domain.repository

import com.neilb.synapcart.data.model.UserProfile

interface UserRepository {
    suspend fun getUserProfile(): Result<UserProfile>
    suspend fun updateProfile(fullName: String?, language: String?, currency: String?): Result<UserProfile>
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun deleteAccount(password: String?): Result<Unit>
    /** Caches a profile returned by another endpoint (store purchase verification). */
    suspend fun applyProfile(profile: UserProfile)
}
