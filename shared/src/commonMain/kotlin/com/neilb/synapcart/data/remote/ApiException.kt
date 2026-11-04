package com.neilb.synapcart.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

/**
 * Typed failure for every backend call. [detail] is the backend's `detail` string when it
 * sent one (already in the user's language). Each platform maps the subclass to a localized
 * fallback message when [detail] is null (Android: `uiText`, iOS: `ApiError.message`).
 */
sealed class ApiException(
    /** The backend's `detail`, if it sent one. */
    val detail: String?,
    cause: Throwable? = null
) : Exception(detail ?: "ApiException", cause) {

    /** No connection, DNS failure, timeout. [cause] is kept for logs and crash reports. */
    class Network(cause: Throwable? = null) : ApiException(null, cause)

    /** 401: the session is no longer valid (the client has already cleared it). */
    class Unauthorized(detail: String? = null) : ApiException(detail)

    /**
     * 402: a plan limit. [code] is the backend's machine code: [CODE_QUOTA] (daily messages used
     * up), [CODE_PREMIUM] (premium-only feature) or [CODE_HISTORY_LOCKED] (old free-plan chat).
     */
    class QuotaExceeded(detail: String? = null, val code: String? = null) : ApiException(detail)

    /** 429: rate limited. */
    class RateLimited(detail: String? = null) : ApiException(detail)

    /** Any other HTTP error (or -1 for an unreadable response). */
    class Http(val code: Int, detail: String? = null) : ApiException(detail)

    /** Client-side validation failed before any request was made; [reason] is one of `REASON_*`. */
    class Validation(val reason: String) : ApiException(null)

    companion object {
        const val CODE_QUOTA = "quota_exceeded"
        const val CODE_PREMIUM = "premium_required"
        const val CODE_HISTORY_LOCKED = "history_locked"

        const val REASON_PRODUCT_NO_LINK = "product_no_link"

        /** Extracts `detail` from an error body; null if absent or not a string. */
        fun parseDetail(body: String?): String? = parseStringField(body, "detail")

        /** The machine-readable `code` of a plan error body, if any. */
        fun parseCode(body: String?): String? = parseStringField(body, "code")

        private fun parseStringField(body: String?, name: String): String? {
            if (body.isNullOrBlank()) return null
            return try {
                val value = Json.parseToJsonElement(body).jsonObject[name]
                (value as? JsonPrimitive)?.takeIf { it.isString }?.content?.takeIf { it.isNotBlank() }
            } catch (_: Exception) {
                null
            }
        }

        fun fromHttp(code: Int, body: String?): ApiException {
            val detail = parseDetail(body)
            return when (code) {
                401 -> Unauthorized(detail)
                402 -> QuotaExceeded(detail, parseCode(body))
                429 -> RateLimited(detail)
                else -> Http(code, detail)
            }
        }
    }
}

/** Maps any failure of a backend call to an [ApiException]. */
fun Throwable.asApiException(): ApiException = when (this) {
    is ApiException -> this
    is SerializationException -> ApiException.Http(-1)
    is IllegalArgumentException -> ApiException.Http(-1)
    // IOExceptions, timeouts, Darwin URL errors: all network-level.
    else -> ApiException.Network(this)
}

/** Runs a backend call and maps every failure to an [ApiException] inside [Result]. */
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e.asApiException())
    }
}
