package com.neilb.synapcart.data.repository

import com.neilb.synapcart.data.model.AgentStreamEvent
import com.neilb.synapcart.data.remote.ApiException
import com.neilb.synapcart.data.remote.SynapCartApi
import com.neilb.synapcart.data.remote.asApiException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

interface ChatStreamRepository {
    /**
     * Streams the agent's SSE events for one message. The flow completes after a final
     * event (completed / out_of_scope / error) and fails with an [ApiException] on HTTP or
     * network errors. Cancelling the collector cancels the HTTP call.
     */
    fun streamAgent(sessionId: Int, message: String): Flow<AgentStreamEvent>
}

class ChatStreamRepositoryImpl(private val api: SynapCartApi) : ChatStreamRepository {
    override fun streamAgent(sessionId: Int, message: String): Flow<AgentStreamEvent> =
        api.streamAgent(sessionId, message).catch { e ->
            if (e is CancellationException) throw e
            throw e.asApiException()
        }
}
