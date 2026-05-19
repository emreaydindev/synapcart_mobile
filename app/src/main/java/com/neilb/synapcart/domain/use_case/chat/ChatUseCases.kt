package com.neilb.synapcart.domain.use_case.chat

data class ChatUseCases(
    val getSessions: GetSessionsUseCase,
    val sendMessage: SendMessageUseCase,
    val createSession: CreateSessionUseCase,
    val getMessages: GetMessagesUseCase
)