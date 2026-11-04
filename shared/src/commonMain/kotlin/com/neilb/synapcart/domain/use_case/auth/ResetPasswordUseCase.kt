package com.neilb.synapcart.domain.use_case.auth

import com.neilb.synapcart.domain.repository.AuthRepository
import com.neilb.synapcart.util.PasswordPolicy

class ResetPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(token: String, newPassword: String): Result<Unit> {
        PasswordPolicy.check(newPassword)?.let { return Result.failure(com.neilb.synapcart.data.remote.ApiException.Validation(it)) }
        return repository.resetPassword(token, newPassword)
    }
}
