package com.neilb.synapcart.domain.use_case.chat

import com.neilb.synapcart.domain.model.ChatMessage
import com.neilb.synapcart.domain.repository.SynapCartRepository

class GetMessagesUseCase(private val repository: SynapCartRepository) {
    suspend operator fun invoke(sessionId: Int): Result<List<ChatMessage>> {
        return repository.getMessages(sessionId)
    }
}