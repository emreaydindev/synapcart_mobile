package com.neilb.synapcart.data.repository

import com.neilb.synapcart.data.model.ChatMessageResponse
import com.neilb.synapcart.data.model.SessionResponse
import com.neilb.synapcart.data.model.SessionUpdateRequest
import com.neilb.synapcart.data.remote.SynapCartApiService
import com.neilb.synapcart.data.remote.requireSuccess
import com.neilb.synapcart.data.remote.safeApiCall
import com.neilb.synapcart.domain.model.ChatMessage
import com.neilb.synapcart.domain.repository.SynapCartRepository
import com.neilb.synapcart.util.parseServerTimestamp

class SynapCartRepositoryImpl(
    private val apiService: SynapCartApiService
) : SynapCartRepository {

    override suspend fun getSessions(): Result<List<SessionResponse>> = safeApiCall {
        apiService.getSessions()
    }

    override suspend fun createSession(): Result<SessionResponse> = safeApiCall {
        apiService.createSession()
    }

    override suspend fun renameSession(sessionId: Int, title: String): Result<SessionResponse> = safeApiCall {
        apiService.renameSession(sessionId, SessionUpdateRequest(title.trim()))
    }

    override suspend fun deleteSession(sessionId: Int): Result<Unit> = safeApiCall {
        apiService.deleteSession(sessionId).requireSuccess()
        Unit
    }

    override suspend fun getMessages(sessionId: Int): Result<List<ChatMessage>> = safeApiCall {
        apiService.getMessages(sessionId).map { it.toDomain() }
    }

    private fun ChatMessageResponse.toDomain() = ChatMessage(
        id = id.toString(),
        text = content.orEmpty(),
        isUser = role != "assistant",
        timestamp = parseServerTimestamp(createdAt) ?: System.currentTimeMillis(),
        products = products.orEmpty()
    )
}
