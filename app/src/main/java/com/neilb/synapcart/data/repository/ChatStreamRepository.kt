package com.neilb.synapcart.data.repository

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.neilb.synapcart.data.model.AgentStreamEvent
import com.neilb.synapcart.data.model.ChatRequest
import com.neilb.synapcart.data.remote.ApiException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

interface ChatStreamRepository {
    /**
     * Streams the agent's SSE events for one message. The flow completes after a final
     * event (completed / out_of_scope / error) and fails with an [ApiException] on HTTP or
     * network errors. Cancelling the collector cancels the HTTP call.
     */
    fun streamAgent(sessionId: Int, message: String): Flow<AgentStreamEvent>
}

class ChatStreamRepositoryImpl(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
    /** Backend origin with a trailing slash (BuildConfig.BASE_URL). */
    private val baseUrl: String
) : ChatStreamRepository {

    override fun streamAgent(sessionId: Int, message: String): Flow<AgentStreamEvent> = callbackFlow {
        val request = Request.Builder()
            .url("${baseUrl}api/v1/chat/agent/$sessionId/stream")
            .header("Accept", "text/event-stream")
            .post(gson.toJson(ChatRequest(message)).toRequestBody(JSON))
            .build()
        val call = okHttpClient.newCall(request)

        launch {
            try {
                call.execute().use { response ->
                    if (!response.isSuccessful) {
                        throw ApiException.fromHttp(response.code, response.body.string())
                    }
                    val source = response.body.source()
                    while (isActive) {
                        val line = source.readUtf8Line() ?: break
                        val payload = line.trim().removePrefix("data:").trim()
                        if (payload.isEmpty() || line.startsWith(":")) continue
                        val event = try {
                            gson.fromJson(payload, AgentStreamEvent::class.java)
                        } catch (_: JsonSyntaxException) {
                            null
                        } ?: continue
                        send(event)
                        if (event.isFinal) break
                    }
                }
                close()
            } catch (e: ApiException) {
                close(e)
            } catch (e: IOException) {
                close(if (call.isCanceled()) null else ApiException.Network())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                close(ApiException.Http(-1))
            }
        }

        awaitClose { call.cancel() }
    }.flowOn(Dispatchers.IO)

    private companion object {
        val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
