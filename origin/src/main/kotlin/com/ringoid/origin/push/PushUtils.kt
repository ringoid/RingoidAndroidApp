package com.ringoid.origin.push

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ringoid.origin.R
import timber.log.Timber

object PushUtils {

    private var NOTIFICATION_ID = 0

    /**
     * Extra flag for [Intent] to open activity via deeplink, indicating that the activity
     * has been open as deeplink from push notification.
     */
    const val PUSH_NOTIFICATION_OPEN = "push_notification_open"

    fun createNotification(context: Context, title: String? = null, text: String? = null,
                           pendingIntent: PendingIntent? = null): Int {
        val channelId = context.resources.getString(R.string.channel_id_default)
        val nm = NotificationManagerCompat.from(context)
        val notificationId = NOTIFICATION_ID++
        val notification = NotificationCompat.Builder(context, channelId)
            .setAutoCancel(true)
            .setContentText(text)
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_push_notification_24dp)
            .build()
        nm.notify(notificationId, notification)
        return notificationId
    }

    fun hideNotification(context: Context, notificationId: Int) {
        val nm = NotificationManagerCompat.from(context)
        nm.cancel(notificationId)
    }
}
