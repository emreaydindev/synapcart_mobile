package com.neilb.synapcart.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.neilb.synapcart.MainActivity
import com.neilb.synapcart.R

/**
 * Price-alert notifications. The backend sends FCM messages with a `notification` block and
 * data `{type: "price_alert", favorite_id, link, price}`. In the background Android shows them
 * itself and the tap opens [MainActivity] with the data as intent extras; in the foreground
 * [show] builds the same notification. Either way MainActivity opens the product.
 */
object PriceAlertNotifications {
    /** Must match the backend's `push_service.ANDROID_CHANNEL_ID`. */
    const val CHANNEL_ID = "price_alerts"
    const val TYPE_PRICE_ALERT = "price_alert"
    const val EXTRA_TYPE = "type"
    const val EXTRA_LINK = "link"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_price_alerts),
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = context.getString(R.string.notification_channel_price_alerts_desc) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun canPost(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun show(context: Context, title: String, body: String, data: Map<String, String>) {
        if (!canPost(context)) return
        val link = data[EXTRA_LINK] ?: return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_TYPE, TYPE_PRICE_ALERT)
            putExtra(EXTRA_LINK, link)
        }
        val pending = PendingIntent.getActivity(
            context,
            link.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(link.hashCode(), notification)
        } catch (_: SecurityException) {
            // Permission revoked between the check and the post.
        }
    }

    /** The product link of a price-alert intent, or null for any other intent. */
    fun linkFrom(intent: Intent?): String? =
        intent?.takeIf { it.getStringExtra(EXTRA_TYPE) == TYPE_PRICE_ALERT }?.getStringExtra(EXTRA_LINK)
}
