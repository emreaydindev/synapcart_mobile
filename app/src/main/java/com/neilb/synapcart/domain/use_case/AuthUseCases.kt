package com.neilb.synapcart.domain.use_case

data class AuthUseCases(
    val login: LoginUseCase,
    val register: RegisterUseCase,
    val forgotPassword: ForgotPasswordUseCase,
    val resetPassword: ResetPasswordUseCase
)