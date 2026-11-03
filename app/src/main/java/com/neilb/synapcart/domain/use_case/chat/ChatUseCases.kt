package com.neilb.synapcart.domain.use_case.chat

import com.neilb.synapcart.domain.repository.SynapCartRepository

class RenameSessionUseCase(private val repository: SynapCartRepository) {
    suspend operator fun invoke(sessionId: Int, title: String) = repository.renameSession(sessionId, title)
}

class DeleteSessionUseCase(private val repository: SynapCartRepository) {
    suspend operator fun invoke(sessionId: Int) = repository.deleteSession(sessionId)
}

data class ChatUseCases(
    val getSessions: GetSessionsUseCase,
    val createSession: CreateSessionUseCase,
    val renameSession: RenameSessionUseCase,
    val deleteSession: DeleteSessionUseCase,
    val getMessages: GetMessagesUseCase
)
