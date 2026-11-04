package com.neilb.synapcart.domain.use_case.auth

import com.neilb.synapcart.data.remote.ApiException
import com.neilb.synapcart.util.ValidationReasons
import com.neilb.synapcart.domain.repository.AuthRepository

class ForgotPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(ApiException.Validation(ValidationReasons.EMAIL_EMPTY))
        }
        return repository.forgotPassword(email)
    }
}
