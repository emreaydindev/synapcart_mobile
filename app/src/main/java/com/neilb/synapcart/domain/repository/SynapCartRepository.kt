package com.neilb.synapcart.domain.repository

import com.neilb.synapcart.data.model.AgentResponse
import com.neilb.synapcart.data.model.SessionResponse
import com.neilb.synapcart.domain.model.ChatMessage

interface SynapCartRepository {
    suspend fun sendMessage(sessionId: Int, message: String): Result<AgentResponse>
    suspend fun getSessions(): Result<List<SessionResponse>>
    suspend fun createSession(): Result<SessionResponse>
    suspend fun getMessages(sessionId: Int): Result<List<ChatMessage>>
}