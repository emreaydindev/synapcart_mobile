package com.neilb.synapcart.data.remote

import com.neilb.synapcart.data.model.AccountDeleteRequest
import com.neilb.synapcart.data.model.AddFavoriteRequest
import com.neilb.synapcart.data.model.AgentStreamEvent
import com.neilb.synapcart.data.model.AppleVerifyRequest
import com.neilb.synapcart.data.model.AuthResponse
import com.neilb.synapcart.data.model.ChatMessageResponse
import com.neilb.synapcart.data.model.ChatRequest
import com.neilb.synapcart.data.model.CompareRequest
import com.neilb.synapcart.data.model.CompareResponse
import com.neilb.synapcart.data.model.DeviceTokenRequest
import com.neilb.synapcart.data.model.FavoriteResponse
import com.neilb.synapcart.data.model.FavoriteUpdateRequest
import com.neilb.synapcart.data.model.ForgotPasswordRequest
import com.neilb.synapcart.data.model.LoginRequest
import com.neilb.synapcart.data.model.MessageResponseBody
import com.neilb.synapcart.data.model.PasswordChangeRequest
import com.neilb.synapcart.data.model.RegisterRequest
import com.neilb.synapcart.data.model.ResetPasswordRequest
import com.neilb.synapcart.data.model.SessionResponse
import com.neilb.synapcart.data.model.SessionUpdateRequest
import com.neilb.synapcart.data.model.UserProfile
import com.neilb.synapcart.data.model.UserProfileUpdateRequest
import com.neilb.synapcart.data.model.VerifyPurchaseRequest
import com.neilb.synapcart.util.SessionEvents
import com.neilb.synapcart.util.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.readLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/** JSON settings shared by the client and tests. */
val SynapJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}

/**
 * The SynapCart REST API (synapcart_backend, `/api/v1`). Adds `Authorization: Bearer` to
 * every request except `/auth/` ones. When an authenticated request returns 401 (expired or
 * revoked token) the stored session is cleared and [SessionEvents.expired] fires; a 401 from
 * `/auth/login` just means a wrong password. Non-2xx responses throw [ApiException].
 */
class SynapCartApi(
    /** Backend origin, e.g. `https://api.synapcart.emreaydin.dev/` (trailing slash optional). */
    baseUrl: String,
    private val sessionManager: SessionManager,
    private val sessionEvents: SessionEvents,
    engine: HttpClientEngine = defaultHttpEngine(),
    /** Debug builds only: logs requests with the Authorization header redacted (never bodies). */
    logRequests: Boolean = false
) {
    private val base = baseUrl.trimEnd('/')

    private val client = HttpClient(engine) {
        expectSuccess = false
        install(ContentNegotiation) { json(SynapJson) }
        install(HttpTimeout) {
            connectTimeoutMillis = 30_000
            // The agent can take a while before the first byte.
            requestTimeoutMillis = 120_000
            socketTimeoutMillis = 120_000
        }
        if (logRequests) {
            install(Logging) {
                level = LogLevel.INFO
                sanitizeHeader { it == HttpHeaders.Authorization }
            }
        }
    }

    private suspend fun HttpRequestBuilder.prepare(method: HttpMethod, path: String): String? {
        this.method = method
        url("$base/api/v1/$path")
        if (path.startsWith("auth/")) return null
        val token = sessionManager.authToken.firstOrNull()?.takeIf { it.isNotBlank() } ?: return null
        bearerAuth(token)
        return token
    }

    /** Clears the session after a 401 on an authenticated call, unless a fresh login raced it. */
    private suspend fun handleUnauthorized(sentToken: String?) {
        if (sentToken != null && sessionManager.authToken.firstOrNull() == sentToken) {
            sessionManager.clearSession()
            sessionEvents.notifyExpired()
        }
    }

    private suspend fun send(method: HttpMethod, path: String, body: Any? = null, query: Map<String, String> = emptyMap()): HttpResponse {
        var sent: String? = null
        val response = client.request {
            sent = prepare(method, path)
            query.forEach { (k, v) -> parameter(k, v) }
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
        if (!response.status.isSuccess()) {
            if (response.status.value == 401) handleUnauthorized(sent)
            throw ApiException.fromHttp(response.status.value, runCatching { response.bodyAsText() }.getOrNull())
        }
        return response
    }

    private suspend inline fun <reified T> call(method: HttpMethod, path: String, body: Any? = null, query: Map<String, String> = emptyMap()): T {
        val text = send(method, path, body, query).bodyAsText()
        return SynapJson.decodeFromString(text)
    }

    // --- Auth ---

    suspend fun login(request: LoginRequest): AuthResponse = call(HttpMethod.Post, "auth/login", request)

    /** Returns the created user (201), not a token; log in afterwards. */
    suspend fun register(request: RegisterRequest): UserProfile = call(HttpMethod.Post, "auth/register", request)

    suspend fun forgotPassword(request: ForgotPasswordRequest): MessageResponseBody =
        call(HttpMethod.Post, "auth/forgot-password", request)

    suspend fun resetPassword(request: ResetPasswordRequest): MessageResponseBody =
        call(HttpMethod.Post, "auth/reset-password", request)

    // --- Chat ---

    suspend fun createSession(): SessionResponse = call(HttpMethod.Post, "chat/sessions")

    suspend fun getSessions(): List<SessionResponse> = call(HttpMethod.Get, "chat/sessions")

    suspend fun renameSession(sessionId: Int, request: SessionUpdateRequest): SessionResponse =
        call(HttpMethod.Patch, "chat/sessions/$sessionId", request)

    suspend fun deleteSession(sessionId: Int) {
        send(HttpMethod.Delete, "chat/sessions/$sessionId")
    }

    suspend fun getMessages(sessionId: Int): List<ChatMessageResponse> =
        call(HttpMethod.Get, "chat/agent/$sessionId/messages")

    /** AI comparison of 2–4 products. Premium only (402 `premium_required` otherwise). */
    suspend fun compareProducts(request: CompareRequest): CompareResponse = call(HttpMethod.Post, "chat/compare", request)

    /**
     * Streams the agent's SSE events for one message. Completes after a final event
     * (completed / out_of_scope / error) and throws [ApiException] on HTTP errors.
     * Cancelling the collector cancels the HTTP call.
     */
    fun streamAgent(sessionId: Int, message: String): Flow<AgentStreamEvent> = flow {
        var sent: String? = null
        val statement = client.prepareRequest {
            sent = prepare(HttpMethod.Post, "chat/agent/$sessionId/stream")
            accept(ContentType.Text.EventStream)
            contentType(ContentType.Application.Json)
            setBody(ChatRequest(message))
        }
        statement.execute { response ->
            if (!response.status.isSuccess()) {
                if (response.status.value == 401) handleUnauthorized(sent)
                throw ApiException.fromHttp(response.status.value, runCatching { response.bodyAsText() }.getOrNull())
            }
            val channel = response.bodyAsChannel()
            while (true) {
                // Non-strict reader: LF or CRLF, and end of stream counts as a line break.
                // (The deprecated readUTF8Line relied on matching an EOFException message,
                // which failed on Kotlin/Native.)
                val line = channel.readLine() ?: break
                val event = parseSseLine(line) ?: continue
                emit(event)
                if (event.isFinal) break
            }
        }
    }

    // --- Favorites ---

    suspend fun getFavorites(): List<FavoriteResponse> = call(HttpMethod.Get, "favorites/")

    /** Idempotent: 201 for a new favorite, 200 with the existing row for a duplicate link. */
    suspend fun addFavorite(request: AddFavoriteRequest): FavoriteResponse = call(HttpMethod.Post, "favorites/", request)

    suspend fun removeFavorite(favoriteId: Int) {
        send(HttpMethod.Delete, "favorites/$favoriteId")
    }

    suspend fun removeFavoriteByLink(link: String) {
        send(HttpMethod.Delete, "favorites/by-link", query = mapOf("link" to link))
    }

    /** Sets or clears (`target_price: null`) the price alert. Premium only (402 otherwise). */
    suspend fun updateFavorite(favoriteId: Int, request: FavoriteUpdateRequest): FavoriteResponse =
        call(HttpMethod.Patch, "favorites/$favoriteId", request)

    // --- User ---

    suspend fun getUserProfile(): UserProfile = call(HttpMethod.Get, "user/me")

    suspend fun updateProfile(request: UserProfileUpdateRequest): UserProfile = call(HttpMethod.Patch, "user/me", request)

    /** Returns a fresh token; all other tokens are revoked. */
    suspend fun changePassword(request: PasswordChangeRequest): AuthResponse = call(HttpMethod.Post, "user/me/password", request)

    suspend fun deleteAccount(request: AccountDeleteRequest) {
        send(HttpMethod.Delete, "user/me", request)
    }

    /** Registers this device's FCM token for price alerts (idempotent). */
    suspend fun registerDeviceToken(request: DeviceTokenRequest) {
        send(HttpMethod.Post, "user/me/device-token", request)
    }

    suspend fun unregisterDeviceToken(request: DeviceTokenRequest) {
        send(HttpMethod.Delete, "user/me/device-token", request)
    }

    // --- Billing ---

    /** Validates a Play subscription purchase on the server; returns the updated profile. */
    suspend fun verifyGooglePurchase(request: VerifyPurchaseRequest): UserProfile =
        call(HttpMethod.Post, "billing/google/verify", request)

    /** Validates a StoreKit 2 signed transaction on the server; returns the updated profile. */
    suspend fun verifyApplePurchase(request: AppleVerifyRequest): UserProfile =
        call(HttpMethod.Post, "billing/apple/verify", request)

    /** True if a token is stored (used before calls that only make sense when signed in). */
    suspend fun isSignedIn(): Boolean = !sessionManager.authToken.first().isNullOrBlank()

    fun close() = client.close()
}

/** Parses one SSE line into an event; comments, blank lines and bad JSON yield null. */
internal fun parseSseLine(line: String): AgentStreamEvent? {
    if (line.startsWith(":")) return null
    val payload = line.trim().removePrefix("data:").trim()
    if (payload.isEmpty()) return null
    return try {
        SynapJson.decodeFromString<AgentStreamEvent>(payload)
    } catch (_: Exception) {
        null
    }
}

/** OkHttp on Android, Darwin (NSURLSession) on iOS. */
expect fun defaultHttpEngine(): HttpClientEngine
