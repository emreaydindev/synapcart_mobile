package com.neilb.synapcart.data.remote.interceptor

import com.neilb.synapcart.util.SessionManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val isPublicEndpoint = originalRequest.url.encodedPath.contains("/auth/") &&
                !originalRequest.url.encodedPath.contains("/logout")

        if (isPublicEndpoint) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { sessionManager.authToken.firstOrNull() }

        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}