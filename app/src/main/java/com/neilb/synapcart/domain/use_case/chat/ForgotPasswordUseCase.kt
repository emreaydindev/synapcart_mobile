package com.neilb.synapcart.domain.use_case.chat

import com.neilb.synapcart.domain.repository.AuthRepository

class ForgotPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(Exception("E-posta boş bırakılamaz."))
        }
        return repository.forgotPassword(email)
    }
}
