package com.neilb.synapcart.domain.use_case

import com.neilb.synapcart.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(fullName: String, email: String, password: String): Result<Unit> {
        if (fullName.isBlank()) {
            return Result.failure(Exception("İsim boş bırakılamaz."))
        }
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("E-posta ve şifre boş bırakılamaz."))
        }
        return repository.register(email, password, fullName)
    }
}