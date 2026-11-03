package com.neilb.synapcart.data.remote

import com.neilb.synapcart.data.model.ChatMessageResponse
import com.neilb.synapcart.data.model.SessionResponse
import com.neilb.synapcart.data.model.SessionUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** Chat sessions. The agent itself is consumed via SSE in ChatStreamRepository. */
interface SynapCartApiService {

    @POST("api/v1/chat/sessions")
    suspend fun createSession(): SessionResponse

    @GET("api/v1/chat/sessions")
    suspend fun getSessions(): List<SessionResponse>

    @PATCH("api/v1/chat/sessions/{session_id}")
    suspend fun renameSession(
        @Path("session_id") sessionId: Int,
        @Body request: SessionUpdateRequest
    ): SessionResponse

    @DELETE("api/v1/chat/sessions/{session_id}")
    suspend fun deleteSession(@Path("session_id") sessionId: Int): Response<Unit>

    @GET("api/v1/chat/agent/{session_id}/messages")
    suspend fun getMessages(@Path("session_id") sessionId: Int): List<ChatMessageResponse>
}
