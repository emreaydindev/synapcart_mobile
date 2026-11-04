package com.neilb.synapcart

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.neilb.synapcart.data.model.AgentStreamEvent
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.data.remote.ApiException
import com.neilb.synapcart.data.remote.SynapJson
import com.neilb.synapcart.shared.SynapCart
import com.neilb.synapcart.util.PasswordPolicy
import com.neilb.synapcart.util.SessionGroup
import com.neilb.synapcart.util.ValidationReasons
import com.neilb.synapcart.util.parsePriceInput
import com.neilb.synapcart.util.parseServerTimestamp
import com.neilb.synapcart.util.sessionGroupOf
import com.neilb.synapcart.util.suggestTargetPrice
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Ktor's HttpTimeout would fire instantly under runTest's virtual time, so run on a real dispatcher. */
private fun realTimeTest(block: suspend kotlinx.coroutines.CoroutineScope.() -> Unit) =
    runTest { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { block() } }

/** In-memory DataStore so tests need no file system. */
private class MemoryDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())
    override val data = state
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val next = transform(state.value)
        state.value = next
        return next
    }
}

private fun json(body: String, status: HttpStatusCode = HttpStatusCode.OK): suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = {
    respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
}

private fun sdk(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): Pair<SynapCart, MutableList<HttpRequestData>> {
    val seen = mutableListOf<HttpRequestData>()
    val engine = MockEngine { request ->
        seen += request
        handler(request)
    }
    return SynapCart("https://api.test/", MemoryDataStore(), engine = engine) to seen
}

private fun HttpRequestData.bodyText(): String = when (val content = body) {
    is io.ktor.http.content.TextContent -> content.text
    is OutgoingContent.ByteArrayContent -> content.bytes().decodeToString()
    else -> ""
}

class ChatStreamTest {
    @Test
    fun parsesEventsUntilTheFinalOne() = realTimeTest {
        val sse = listOf(
            """data: {"status":"routing"}""",
            "",
            ": keep-alive comment",
            """data: {"status":"searching"}""",
            "",
            """data: {"status":"thinking","products":[{"title":"Phone A","price":100.0,"link":"https://a"}]}""",
            "",
            "data: not json",
            "",
            """data: {"status":"completed","analysis":"Best pick","products":[]}""",
            "",
            """data: {"status":"ignored-after-final"}""",
        ).joinToString("\n")
        val (app, seen) = sdk { respond(sse, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "text/event-stream")) }
        app.session.saveAuthToken("tok", "Ada")

        val events = app.stream.streamAgent(7, "cheap phone").toList()

        assertEquals(listOf("routing", "searching", "thinking", "completed"), events.map { it.status })
        assertEquals("Phone A", events[2].products!!.single().title)
        assertTrue(events.last().isFinal)
        assertEquals("Best pick", events.last().analysis)
        val request = seen.single()
        assertEquals("https://api.test/api/v1/chat/agent/7/stream", request.url.toString())
        assertEquals("Bearer tok", request.headers[HttpHeaders.Authorization])
        assertEquals("cheap phone", SynapJson.parseToJsonElement(request.bodyText()).jsonObject["message"]!!.jsonPrimitive.content)
    }

    @Test
    fun mapsQuotaErrorsWithTheirCode() = realTimeTest {
        val (app, _) = sdk(json("""{"detail":"Bugünkü 5 mesaj hakkını kullandın.","code":"quota_exceeded"}""", HttpStatusCode.PaymentRequired))
        val error = assertFailsWith<ApiException.QuotaExceeded> { app.stream.streamAgent(1, "x").toList() }
        assertEquals(ApiException.CODE_QUOTA, error.code)
        assertEquals("Bugünkü 5 mesaj hakkını kullandın.", error.detail)
    }

    @Test
    fun unauthorizedClearsTheSessionAndSignalsExpiry() = realTimeTest {
        val (app, _) = sdk(json("""{"detail":"Oturumunuzun süresi doldu."}""", HttpStatusCode.Unauthorized))
        app.session.saveAuthToken("old", "Ada")
        val result = app.chat.getSessions()
        assertIs<ApiException.Unauthorized>(result.exceptionOrNull())
        assertNull(app.session.authToken.first())
    }
}

class RepositoryTest {
    @Test
    fun loginStoresTheTokenAndAuthCallsSendNoBearer() = realTimeTest {
        val (app, seen) = sdk(json("""{"access_token":"jwt","token_type":"bearer","user_name":"Ada"}"""))
        assertTrue(app.auth.login(" ada@example.com ", "secret123").isSuccess)
        assertEquals("jwt", app.session.authToken.first())
        assertEquals("Ada", app.session.userName.first())
        assertNull(seen.single().headers[HttpHeaders.Authorization])
        assertTrue(seen.single().bodyText().contains("\"email\":\"ada@example.com\""))
    }

    @Test
    fun wrongPasswordDoesNotClearAnything() = realTimeTest {
        val (app, _) = sdk(json("""{"detail":"Hatalı email veya şifre."}""", HttpStatusCode.Unauthorized))
        val error = app.auth.login("a@b.com", "x").exceptionOrNull()
        assertIs<ApiException.Unauthorized>(error)
        assertEquals("Hatalı email veya şifre.", error.detail)
    }

    @Test
    fun clearingAPriceAlertSendsAnExplicitNull() = realTimeTest {
        val (app, seen) = sdk { request ->
            if (request.url.encodedPath.endsWith("/favorites/3")) {
                respond("""{"id":3,"title":"X","link":"https://x","target_price":null}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            } else error("unexpected ${request.url}")
        }
        app.session.saveAuthToken("tok", null)
        assertTrue(app.favorites.setTargetPrice(3, null).isSuccess)
        assertEquals("""{"target_price":null}""", seen.single().bodyText())
    }

    @Test
    fun toggleRequiresALink() = realTimeTest {
        val (app, _) = sdk { error("no request expected") }
        val error = app.favorites.toggle(ProductDTO(title = "No link")).exceptionOrNull()
        assertIs<ApiException.Validation>(error)
        assertEquals(ApiException.REASON_PRODUCT_NO_LINK, error.reason)
    }

    @Test
    fun profileUpdatesThePlanState() = realTimeTest {
        val (app, _) = sdk(
            json("""{"id":"u1","email":"a@b.com","full_name":"Ada","language":"en","currency":"USD","plan":"premium","plan_expires_at":"2027-01-01T00:00:00.000Z","plan_source":"apple","usage_today":2,"daily_limit":100,"unknown_field":true}""")
        )
        app.session.saveAuthToken("tok", null)
        val profile = app.user.getUserProfile().getOrThrow()
        assertTrue(profile.isPremium)
        val plan = app.plan.state.value
        assertTrue(plan.isPremium)
        assertEquals("apple", plan.source)
        assertEquals(100, plan.dailyLimit)
        assertEquals("USD", app.session.currency.first())
        assertTrue(app.session.isPremium.first())
    }

    @Test
    fun networkFailuresBecomeNetworkErrors() = realTimeTest {
        val (app, _) = sdk { throw kotlinx.io.IOException("offline") }
        assertIs<ApiException.Network>(app.chat.getSessions().exceptionOrNull())
    }
}

class RulesTest {
    @Test
    fun passwordPolicyMatchesTheBackend() {
        assertEquals(ValidationReasons.PASSWORD_TOO_SHORT, PasswordPolicy.check("ab1"))
        assertEquals(ValidationReasons.PASSWORD_NEEDS_LETTERS_DIGITS, PasswordPolicy.check("abcdefgh"))
        assertEquals(ValidationReasons.PASSWORD_NEEDS_LETTERS_DIGITS, PasswordPolicy.check("12345678"))
        assertEquals(ValidationReasons.PASSWORD_TOO_LONG, PasswordPolicy.check("a1" + "ş".repeat(40)))
        assertNull(PasswordPolicy.check("secret123"))
    }

    @Test
    fun parsesServerTimestamps() {
        assertEquals(1_767_323_045_123L, parseServerTimestamp("2026-01-02T03:04:05.123Z"))
        assertEquals(1_767_323_045_123L, parseServerTimestamp("2026-01-02T03:04:05.123456"))
        assertEquals(1_767_312_245_000L, parseServerTimestamp("2026-01-02T03:04:05+03:00"))
        assertNull(parseServerTimestamp("yesterday"))
        assertNull(parseServerTimestamp(null))
    }

    @Test
    fun parsesTypedPrices() {
        assertEquals(1299.0, parsePriceInput("1299"))
        assertEquals(1299.9, parsePriceInput("1.299,90"))
        assertEquals(1299.9, parsePriceInput("1,299.90"))
        assertEquals(1299.0, parsePriceInput("1 299"))
        assertEquals(1299.0, parsePriceInput("1 299"))
        assertEquals(12.5, parsePriceInput("12,5"))
        assertNull(parsePriceInput("-5"))
        assertNull(parsePriceInput("abc"))
        assertEquals(899.0, suggestTargetPrice(999.0))
        assertNull(suggestTargetPrice(null))
    }

    @Test
    fun groupsSessionsByDay() {
        val now = parseServerTimestamp("2026-11-04T12:00:00Z")!!
        val day = 86_400_000L
        val utc = TimeZone.UTC
        assertEquals(SessionGroup.TODAY, sessionGroupOf(now - 3_600_000, now, utc))
        assertEquals(SessionGroup.YESTERDAY, sessionGroupOf(now - day, now, utc))
        assertEquals(SessionGroup.LAST_7_DAYS, sessionGroupOf(now - 3 * day, now, utc))
        assertEquals(SessionGroup.LAST_30_DAYS, sessionGroupOf(now - 20 * day, now, utc))
        assertEquals(SessionGroup.OLDER, sessionGroupOf(now - 90 * day, now, utc))
        assertEquals(SessionGroup.OLDER, sessionGroupOf(null, now, utc))
    }

    @Test
    fun parsesErrorBodies() {
        assertEquals("x", ApiException.parseDetail("""{"detail":"x"}"""))
        assertNull(ApiException.parseDetail("""{"detail":[{"msg":"x"}]}"""))
        assertNull(ApiException.parseDetail("<html>"))
        assertIs<ApiException.RateLimited>(ApiException.fromHttp(429, null))
        val http = ApiException.fromHttp(500, "oops")
        assertIs<ApiException.Http>(http)
        assertEquals(500, http.code)
        assertFalse(AgentStreamEvent(status = "thinking").isFinal)
    }
}
