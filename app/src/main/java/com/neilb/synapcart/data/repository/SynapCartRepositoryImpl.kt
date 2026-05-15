package com.neilb.synapcart.data.repository

import com.neilb.synapcart.data.model.*
import com.neilb.synapcart.data.remote.SynapCartApiService
import com.neilb.synapcart.domain.repository.SynapCartRepository

class SynapCartRepositoryImpl(
    private val apiService: SynapCartApiService
) : SynapCartRepository {

    override suspend fun sendMessage(sessionId: Int, message: String): Result<AgentResponse> {
        return try {
            val response = apiService.sendMessage(sessionId, ChatRequest(message))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSessions(): Result<List<SessionResponse>> {
        return try {
            val response = apiService.getSessions()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createSession(): Result<SessionResponse> {
        return try {
            val response = apiService.createSession()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}