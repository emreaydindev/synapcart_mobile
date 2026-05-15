package com.neilb.synapcart.data.remote

import com.neilb.synapcart.data.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SynapCartApiService {

    @POST("api/v1/chat/sessions")
    suspend fun createSession(): SessionResponse

    @GET("api/v1/chat/sessions")
    suspend fun getSessions(): List<SessionResponse>

    @POST("api/v1/chat/agent/{session_id}")
    suspend fun sendMessage(
        @Path("session_id") sessionId: Int,
        @Body request: ChatRequest
    ): AgentResponse
}