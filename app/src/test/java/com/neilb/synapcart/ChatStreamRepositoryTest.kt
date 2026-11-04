package com.neilb.synapcart

import com.google.gson.Gson
import com.neilb.synapcart.data.model.AgentStreamEvent
import com.neilb.synapcart.data.remote.ApiException
import com.neilb.synapcart.data.repository.ChatStreamRepositoryImpl
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ChatStreamRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: ChatStreamRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = ChatStreamRepositoryImpl(OkHttpClient(), Gson(), server.url("/").toString())
    }

    @After
    fun tearDown() {
        server.close()
    }

    private fun sse(vararg events: String) = MockResponse.Builder()
        .code(200)
        .addHeader("Content-Type", "text/event-stream")
        .body(events.joinToString("") { "data: $it\n\n" })
        .build()

    @Test
    fun parsesEventsInOrderAndStopsAtFinal() = runBlocking {
        server.enqueue(
            sse(
                """{"status":"routing"}""",
                """{"status":"searching"}""",
                """{"status":"thinking","products":[{"title":"Laptop","price":1299.5,"link":"https://x.test/1"}]}""",
                """{"status":"completed","analysis":"Bunu öneririm","products":[{"title":"Laptop","price":1299.5,"link":"https://x.test/1"}]}""",
                """{"status":"ignored-after-final"}"""
            )
        )

        val events = repository.streamAgent(7, "laptop öner").toList()

        assertEquals(listOf("routing", "searching", "thinking", "completed"), events.map { it.status })
        assertEquals("Bunu öneririm", events.last().analysis)
        assertEquals(1299.5, events.last().products!!.single().price!!, 0.0)
        assertTrue(events.last().isFinal)

        val request = server.takeRequest()
        assertEquals("/api/v1/chat/agent/7/stream", request.url.encodedPath)
        assertTrue(request.body!!.utf8().contains("laptop öner"))
    }

    @Test
    fun outOfScopeIsFinalWithAnalysis() = runBlocking {
        server.enqueue(sse("""{"status":"routing"}""", """{"status":"out_of_scope","analysis":"Sadece alışveriş"}"""))

        val events = repository.streamAgent(1, "hava nasıl").toList()

        assertEquals(AgentStreamEvent.OUT_OF_SCOPE, events.last().status)
        assertEquals("Sadece alışveriş", events.last().analysis)
    }

    @Test
    fun httpErrorBecomesTypedException() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(429)
                .body("""{"detail":"Çok fazla istek."}""")
                .build()
        )

        try {
            repository.streamAgent(1, "x").toList()
            fail("expected an exception")
        } catch (e: ApiException.RateLimited) {
            assertEquals("Çok fazla istek.", e.message)
        }
    }

    @Test
    fun malformedLinesAreSkipped() = runBlocking {
        server.enqueue(sse("not json", """{"status":"completed","analysis":"ok"}"""))

        val events = repository.streamAgent(1, "x").toList()

        assertEquals(listOf("completed"), events.map { it.status })
    }
}
