package com.neilb.synapcart.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String, fullName: String): Result<Unit>
    suspend fun forgotPassword(email: String): Result<Unit>
    suspend fun resetPassword(token: String, newPassword: String): Result<Unit>
    suspend fun logout()
}