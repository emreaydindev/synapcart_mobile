package com.neilb.synapcart.data.repository

import com.neilb.synapcart.data.model.UserProfileUpdateRequest
import com.neilb.synapcart.data.remote.UserApiService
import com.neilb.synapcart.domain.repository.UserRepository

class UserRepositoryImpl(
    private val apiService: UserApiService
) : UserRepository {

    override suspend fun updateProfile(fullName: String?, language: String?, currency: String?): Result<Unit> {
        return try {
            val request = UserProfileUpdateRequest(fullName, language, currency)
            apiService.updateProfile(request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            apiService.deleteAccount()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}