package com.neilb.synapcart.domain.use_case.auth

import com.neilb.synapcart.R
import com.neilb.synapcart.data.remote.ApiException
import com.neilb.synapcart.domain.repository.AuthRepository
import com.neilb.synapcart.util.PasswordRules

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(fullName: String, email: String, password: String): Result<Unit> {
        if (fullName.isBlank()) {
            return Result.failure(ApiException.Validation(R.string.error_name_empty))
        }
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(ApiException.Validation(R.string.error_email_password_empty))
        }
        PasswordRules.validate(password)?.let { return Result.failure(ApiException.Validation(it)) }
        return repository.register(email, password, fullName)
    }
}
