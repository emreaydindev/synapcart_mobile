package com.neilb.synapcart.data.push

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.neilb.synapcart.data.model.DeviceTokenRequest
import com.neilb.synapcart.data.remote.SynapCartApi
import com.neilb.synapcart.data.remote.safeApiCall
import com.neilb.synapcart.util.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * FCM token registration for price alerts. Firebase is only initialized when the build had
 * `app/google-services.json`; without it every call here is a no-op.
 */
class PushRepository(
    private val context: Context,
    private val userApi: SynapCartApi,
    private val sessionManager: SessionManager
) {
    private var synced = false

    val isAvailable: Boolean
        get() = FirebaseApp.getApps(context).isNotEmpty()

    private suspend fun currentToken(): String? {
        if (!isAvailable) return null
        return FirebaseMessaging.getInstance().token.awaitOrNull()
    }

    /** Sends the device token to the backend (once per process unless [token] is new). */
    suspend fun syncToken(token: String? = null) {
        if (token == null && synced) return
        if (sessionManager.authToken.first().isNullOrBlank()) return
        val value = token ?: currentToken() ?: return
        safeApiCall { userApi.registerDeviceToken(DeviceTokenRequest(value, platform = "android")) }
            .onSuccess { synced = true }
            .onFailure { Log.w(TAG, "Device token registration failed") }
    }

    /** Called before logout so this device stops getting the user's alerts. */
    suspend fun unregister() {
        val value = currentToken() ?: return
        safeApiCall { userApi.unregisterDeviceToken(DeviceTokenRequest(value, platform = "android")) }
        synced = false
    }

    private suspend fun <T> Task<T>.awaitOrNull(): T? = suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (cont.isActive) cont.resume(if (task.isSuccessful) task.result else null)
        }
    }

    companion object {
        private const val TAG = "Push"
    }
}
