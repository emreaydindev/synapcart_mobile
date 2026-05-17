package com.neilb.synapcart.domain.repository

interface UserRepository {
    suspend fun updateProfile(fullName: String?, language: String?, currency: String?): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
}