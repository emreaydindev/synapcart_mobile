package com.neilb.synapcart.domain.use_case.auth

import com.neilb.synapcart.data.remote.ApiException
import com.neilb.synapcart.util.ValidationReasons
import com.neilb.synapcart.domain.repository.AuthRepository
import com.neilb.synapcart.util.PasswordPolicy

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(fullName: String, email: String, password: String): Result<Unit> {
        if (fullName.isBlank()) {
            return Result.failure(ApiException.Validation(ValidationReasons.NAME_EMPTY))
        }
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(ApiException.Validation(ValidationReasons.EMAIL_PASSWORD_EMPTY))
        }
        PasswordPolicy.check(password)?.let { return Result.failure(ApiException.Validation(it)) }
        return repository.register(email, password, fullName)
    }
}
