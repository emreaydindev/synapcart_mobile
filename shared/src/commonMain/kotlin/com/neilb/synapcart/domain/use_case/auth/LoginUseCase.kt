package com.neilb.synapcart.domain.use_case.auth

import com.neilb.synapcart.data.remote.ApiException
import com.neilb.synapcart.util.ValidationReasons
import com.neilb.synapcart.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(ApiException.Validation(ValidationReasons.EMAIL_PASSWORD_EMPTY))
        }
        return repository.login(email, password)
    }
}