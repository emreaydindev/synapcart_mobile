package com.neilb.synapcart.data.repository

import com.google.gson.Gson
import com.neilb.synapcart.data.model.AgentStreamEvent
import com.neilb.synapcart.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSource
import javax.inject.Inject

interface ChatStreamRepository {
    fun streamAgent(sessionId: Int, message: String): Flow<AgentStreamEvent>
}

class ChatStreamRepositoryImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) : ChatStreamRepository {

    override fun streamAgent(sessionId: Int, message: String) = callbackFlow<AgentStreamEvent> {
        val url = "${Constants.BASE_URL}api/v1/chat/agent/$sessionId/stream"
        val json = gson.toJson(mapOf("message" to message))
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val call = okHttpClient.newCall(request)

        val response = try {
            withContext(Dispatchers.IO) { call.execute() }
        } catch (t: Throwable) {
            close(t)
            return@callbackFlow
        }

        val source: BufferedSource = response.body!!.source()

        try {
            while (!source.exhausted()) {
                val line = source.readUtf8Line()
                if (line == null) break
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue
                val payload = if (trimmed.startsWith("data:")) trimmed.removePrefix("data:").trim() else trimmed
                try {
                    val evt = gson.fromJson(payload, AgentStreamEvent::class.java)
                    trySend(evt).isSuccess
                } catch (e: Exception) {
                    // ignore parse errors
                }
            }
            close()
        } catch (t: Throwable) {
            close(t)
        } finally {
            response.close()
        }
    }.flowOn(Dispatchers.IO)
}


