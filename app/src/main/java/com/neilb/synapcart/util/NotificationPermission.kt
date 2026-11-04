package com.neilb.synapcart.util

import android.content.Context
import android.os.Build
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Asks for POST_NOTIFICATIONS (Android 13+) at a moment that explains it: after the first
 * favorite and when a price alert is set. MainActivity collects [requests] and shows the
 * system dialog; the prompt is shown at most once unless [force]d by setting an alert.
 */
class NotificationPermissionController(
    private val context: Context,
    private val sessionManager: SessionManager
) {
    private val _requests = Channel<Unit>(Channel.CONFLATED)
    val requests: Flow<Unit> = _requests.receiveAsFlow()

    suspend fun maybeRequest(force: Boolean = false) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (PriceAlertNotifications.canPost(context)) return
        if (!force && sessionManager.notificationPromptShown.first()) return
        sessionManager.setNotificationPromptShown()
        _requests.send(Unit)
    }
}
