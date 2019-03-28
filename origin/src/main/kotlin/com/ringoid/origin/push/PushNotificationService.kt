package com.ringoid.origin.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ringoid.domain.debug.DebugLogUtil
import timber.log.Timber

class PushNotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage?) {
        super.onMessageReceived(message)
        Timber.d("PUSH: Received push notification [id: ${message?.messageId}] from: ${message?.from}")
        message?.data?.let {
            if (it.isNotEmpty()) {
                val type = parsePushType(it["type"])
                DebugLogUtil.i("PUSH[${type.name.toLowerCase()}]: data = $it")
                when(type) {
                    // TODO: implement handler for each kind of push notification
                    PushType.DEEPLINK -> {}
                    PushType.MESSAGE -> {}
                    PushType.SYSTEM -> {}
                    else -> {}
                }
            } else {
                Timber.v("PUSH: Payload data is empty in push notification")
            }
        }
    }
}
