package com.neilb.synapcart.data.remote

import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Typed failure for every backend call. [message] is always user-presentable: the backend's
 * `detail` string when it sent one, otherwise a generic message for the failure kind.
 */
sealed class ApiException(message: String) : Exception(message) {
    /** No connection, DNS failure, timeout. */
    class Network(message: String = "Sunucuya ulaşılamadı. İnternet bağlantınızı kontrol edin.") : ApiException(message)

    /** 401: the session is no longer valid (the interceptor has already cleared it). */
    class Unauthorized(message: String = "Oturumunuzun süresi doldu. Lütfen tekrar giriş yapın.") : ApiException(message)

    /** 402: daily quota used up / premium-only feature. */
    class QuotaExceeded(message: String = "Günlük kullanım hakkınız doldu.") : ApiException(message)

    /** 429: rate limited. */
    class RateLimited(message: String = "Çok fazla deneme yaptınız. Lütfen biraz sonra tekrar deneyin.") : ApiException(message)

    /** Any other HTTP error. */
    class Http(val code: Int, message: String) : ApiException(message)

    companion object {
        const val GENERIC_MESSAGE = "Sunucu ile iletişimde bir sorun oluştu."

        /** Extracts `detail` from a FastAPI error body; null if absent or not a string. */
        fun parseDetail(body: String?): String? {
            if (body.isNullOrBlank()) return null
            return try {
                val detail = JsonParser.parseString(body).asJsonObject.get("detail")
                if (detail != null && detail.isJsonPrimitive) detail.asString.takeIf { it.isNotBlank() } else null
            } catch (_: Exception) {
                null
            }
        }

        fun fromHttp(code: Int, body: String?): ApiException {
            val detail = parseDetail(body)
            return when (code) {
                401 -> if (detail != null) Unauthorized(detail) else Unauthorized()
                402 -> if (detail != null) QuotaExceeded(detail) else QuotaExceeded()
                429 -> if (detail != null) RateLimited(detail) else RateLimited()
                else -> Http(code, detail ?: GENERIC_MESSAGE)
            }
        }
    }
}

/** Runs a Retrofit call and maps every failure to an [ApiException] inside [Result]. */
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        Result.failure(ApiException.fromHttp(e.code(), e.response()?.errorBody()?.string()))
    } catch (e: IOException) {
        Result.failure(ApiException.Network())
    } catch (e: Exception) {
        Result.failure(ApiException.Http(-1, ApiException.GENERIC_MESSAGE))
    }
}

/** For endpoints declared as `Response<T>` (e.g. 204 No Content): throws on non-2xx so [safeApiCall] maps it. */
fun <T> retrofit2.Response<T>.requireSuccess(): T? {
    if (!isSuccessful) throw HttpException(this)
    return body()
}
