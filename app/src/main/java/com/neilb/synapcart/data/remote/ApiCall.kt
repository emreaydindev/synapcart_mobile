package com.neilb.synapcart.data.remote

import androidx.annotation.StringRes
import com.google.gson.JsonParser
import com.neilb.synapcart.R
import com.neilb.synapcart.util.UiText
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Typed failure for every backend call. [uiText] is always user-presentable: the backend's
 * `detail` string when it sent one (already in the user's language), otherwise a localized
 * generic message for the failure kind.
 */
sealed class ApiException(
    /** The backend's `detail`, if it sent one. */
    val detail: String?,
    @param:StringRes private val fallbackRes: Int
) : Exception(detail ?: "ApiException") {

    val uiText: UiText get() = detail?.let { UiText.Raw(it) } ?: UiText.Res(fallbackRes)

    /** No connection, DNS failure, timeout. */
    class Network : ApiException(null, R.string.error_network)

    /** 401: the session is no longer valid (the interceptor has already cleared it). */
    class Unauthorized(detail: String? = null) : ApiException(detail, R.string.error_unauthorized)

    /**
     * 402: a plan limit. [code] is the backend's machine code: [CODE_QUOTA] (daily messages used
     * up), [CODE_PREMIUM] (premium-only feature) or [CODE_HISTORY_LOCKED] (old free-plan chat).
     */
    class QuotaExceeded(detail: String? = null, val code: String? = null) : ApiException(detail, R.string.error_quota)

    /** 429: rate limited. */
    class RateLimited(detail: String? = null) : ApiException(detail, R.string.error_rate_limited)

    /** Any other HTTP error. */
    class Http(val code: Int, detail: String? = null) : ApiException(detail, R.string.error_generic)

    /** Client-side validation failed before any request was made. */
    class Validation(@StringRes res: Int) : ApiException(null, res)

    companion object {
        const val CODE_QUOTA = "quota_exceeded"
        const val CODE_PREMIUM = "premium_required"
        const val CODE_HISTORY_LOCKED = "history_locked"

        /** Extracts `detail` from a FastAPI error body; null if absent or not a string. */
        fun parseDetail(body: String?): String? = parseStringField(body, "detail")

        /** The machine-readable `code` of a plan error body, if any. */
        fun parseCode(body: String?): String? = parseStringField(body, "code")

        private fun parseStringField(body: String?, name: String): String? {
            if (body.isNullOrBlank()) return null
            return try {
                val value = JsonParser.parseString(body).asJsonObject.get(name)
                if (value != null && value.isJsonPrimitive) value.asString.takeIf { it.isNotBlank() } else null
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

/** The user-facing message for any failure, using [fallback] for non-API errors. */
fun Throwable.toUiText(@StringRes fallback: Int = R.string.error_generic): UiText =
    (this as? ApiException)?.uiText ?: UiText.Res(fallback)

/** Runs a Retrofit call and maps every failure to an [ApiException] inside [Result]. */
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: ApiException) {
        Result.failure(e)
    } catch (e: HttpException) {
        Result.failure(ApiException.fromHttp(e.code(), e.response()?.errorBody()?.string()))
    } catch (e: IOException) {
        Result.failure(ApiException.Network())
    } catch (e: Exception) {
        Result.failure(ApiException.Http(-1))
    }
}

/** For endpoints declared as `Response<T>` (e.g. 204 No Content): throws on non-2xx so [safeApiCall] maps it. */
fun <T> retrofit2.Response<T>.requireSuccess(): T? {
    if (!isSuccessful) throw HttpException(this)
    return body()
}
