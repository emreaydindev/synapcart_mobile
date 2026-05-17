package com.neilb.synapcart.domain.use_case

import com.neilb.synapcart.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("E-posta ve şifre boş bırakılamaz."))
        }
        return repository.login(email, password)
    }
}