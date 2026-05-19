package com.neilb.synapcart.domain.use_case.user

import com.neilb.synapcart.domain.repository.UserRepository

class GetUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke() = repository.getUserProfile()
}

class UpdateProfileUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(fullName: String?, language: String?, currency: String?) =
        repository.updateProfile(fullName, language, currency)
}

class DeleteAccountUseCase(private val repository: UserRepository) {
    suspend operator fun invoke() = repository.deleteAccount()
}

data class UserUseCases(
    val getUser: GetUserUseCase,
    val updateProfile: UpdateProfileUseCase,
    val deleteAccount: DeleteAccountUseCase
)