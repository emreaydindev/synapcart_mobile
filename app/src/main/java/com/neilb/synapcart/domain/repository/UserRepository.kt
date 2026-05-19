package com.neilb.synapcart.domain.repository

import com.neilb.synapcart.data.model.UserProfile

interface UserRepository {

    suspend fun getUserProfile(): Result<UserProfile>
    suspend fun updateProfile(fullName: String?, language: String?, currency: String?): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
}