package com.neilb.synapcart.domain.use_case

data class ChatUseCases(
    val getSessions: GetSessionsUseCase,
    val sendMessage: SendMessageUseCase,
    val createSession: CreateSessionUseCase
)