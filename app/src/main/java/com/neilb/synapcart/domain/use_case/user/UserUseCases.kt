package com.neilb.synapcart.domain.use_case.user

import com.neilb.synapcart.R
import com.neilb.synapcart.data.remote.ApiException
import com.neilb.synapcart.domain.repository.UserRepository
import com.neilb.synapcart.util.PasswordRules

class GetUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke() = repository.getUserProfile()
}

class UpdateProfileUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(fullName: String?, language: String?, currency: String?) =
        repository.updateProfile(fullName, language, currency)
}

class ChangePasswordUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(currentPassword: String, newPassword: String): Result<Unit> {
        if (currentPassword.isBlank() || newPassword.isBlank()) {
            return Result.failure(ApiException.Validation(R.string.error_password_fields_empty))
        }
        PasswordRules.validate(newPassword)?.let { return Result.failure(ApiException.Validation(it)) }
        return repository.changePassword(currentPassword, newPassword)
    }
}

class DeleteAccountUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(password: String?) = repository.deleteAccount(password)
}

data class UserUseCases(
    val getUser: GetUserUseCase,
    val updateProfile: UpdateProfileUseCase,
    val changePassword: ChangePasswordUseCase,
    val deleteAccount: DeleteAccountUseCase
)
