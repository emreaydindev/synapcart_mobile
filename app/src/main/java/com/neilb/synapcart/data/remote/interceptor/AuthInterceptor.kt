package com.neilb.synapcart.data.remote.interceptor

import com.neilb.synapcart.util.SessionEvents
import com.neilb.synapcart.util.SessionManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds the bearer token to every non-auth request. When an authenticated request comes
 * back 401 (token expired or revoked by a password change), the stored session is cleared
 * and [SessionEvents.expired] fires so the UI can return to the login screen.
 *
 * `/auth/` endpoints are excluded both ways: they never carry a token, and a 401 from
 * `/auth/login` just means a wrong password.
 */
class AuthInterceptor(
    private val sessionManager: SessionManager,
    private val sessionEvents: SessionEvents
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        if (originalRequest.url.encodedPath.contains("/auth/")) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { sessionManager.authToken.firstOrNull() }
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        val response = chain.proceed(
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        )

        if (response.code == 401) {
            // Only clear if the token we sent is still the stored one (avoid racing a fresh login).
            runBlocking {
                if (sessionManager.authToken.firstOrNull() == token) {
                    sessionManager.clearSession()
                    sessionEvents.notifyExpired()
                }
            }
        }
        return response
    }
}
