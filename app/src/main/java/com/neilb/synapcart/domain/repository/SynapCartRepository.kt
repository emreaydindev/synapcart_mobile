package com.neilb.synapcart.domain.repository

import com.neilb.synapcart.data.model.SessionResponse
import com.neilb.synapcart.domain.model.ChatMessage

interface SynapCartRepository {
    suspend fun getSessions(): Result<List<SessionResponse>>
    suspend fun createSession(): Result<SessionResponse>
    suspend fun renameSession(sessionId: Int, title: String): Result<SessionResponse>
    suspend fun deleteSession(sessionId: Int): Result<Unit>
    suspend fun getMessages(sessionId: Int): Result<List<ChatMessage>>
}
