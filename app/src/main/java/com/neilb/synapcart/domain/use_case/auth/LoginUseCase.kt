package com.neilb.synapcart.domain.use_case.auth

import com.neilb.synapcart.R
import com.neilb.synapcart.data.remote.ApiException
import com.neilb.synapcart.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(ApiException.Validation(R.string.error_email_password_empty))
        }
        return repository.login(email, password)
    }
}