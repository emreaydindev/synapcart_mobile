package com.neilb.synapcart.domain.use_case.chat

import com.neilb.synapcart.data.model.AgentResponse
import com.neilb.synapcart.domain.repository.SynapCartRepository

class SendMessageUseCase(private val repository: SynapCartRepository) {
    suspend operator fun invoke(sessionId: Int, message: String): Result<AgentResponse> {
        return repository.sendMessage(sessionId, message)
    }
}