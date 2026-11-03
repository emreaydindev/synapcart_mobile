package com.neilb.synapcart.domain.use_case.auth

import com.neilb.synapcart.domain.repository.AuthRepository
import com.neilb.synapcart.util.PasswordRules

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(fullName: String, email: String, password: String): Result<Unit> {
        if (fullName.isBlank()) {
            return Result.failure(IllegalArgumentException("İsim boş bırakılamaz."))
        }
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("E-posta ve şifre boş bırakılamaz."))
        }
        PasswordRules.validate(password)?.let { return Result.failure(IllegalArgumentException(it)) }
        return repository.register(email, password, fullName)
    }
}
