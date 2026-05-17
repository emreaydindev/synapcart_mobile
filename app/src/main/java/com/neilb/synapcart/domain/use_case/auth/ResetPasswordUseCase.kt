package com.neilb.synapcart.domain.use_case.auth

import com.neilb.synapcart.domain.repository.AuthRepository

class ResetPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(token: String, newPassword: String): Result<Unit> {
        if (newPassword.isBlank()) {
            return Result.failure(Exception("Yeni şifre boş bırakılamaz."))
        }
        return repository.resetPassword(token, newPassword)
    }
}
