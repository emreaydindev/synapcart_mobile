package com.neilb.synapcart.data.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.neilb.synapcart.R
import com.neilb.synapcart.util.PriceAlertNotifications
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SynapCartMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var pushRepository: PushRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch { pushRepository.syncToken(token) }
    }

    /** Only called while the app is in the foreground for notification messages. */
    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data[PriceAlertNotifications.EXTRA_TYPE] != PriceAlertNotifications.TYPE_PRICE_ALERT) return
        PriceAlertNotifications.show(
            this,
            title = message.notification?.title ?: getString(R.string.notification_price_drop_title),
            body = message.notification?.body.orEmpty(),
            data = message.data
        )
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
