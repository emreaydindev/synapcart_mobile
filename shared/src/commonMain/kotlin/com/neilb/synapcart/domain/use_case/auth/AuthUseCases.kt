package com.neilb.synapcart.domain.use_case.auth


data class AuthUseCases(
    val login: LoginUseCase,
    val register: RegisterUseCase,
    val forgotPassword: ForgotPasswordUseCase,
    val resetPassword: ResetPasswordUseCase
)