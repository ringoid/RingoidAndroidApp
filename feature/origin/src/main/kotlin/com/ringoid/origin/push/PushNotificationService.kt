package com.ringoid.origin.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ringoid.base.IBaseRingoidApplication
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.model.essence.push.PushTokenEssenceUnauthorized
import com.ringoid.domain.model.push.PushNotificationData
import com.ringoid.report.log.breadcrumb
import timber.log.Timber

class PushNotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("PUSH: Received push notification [id: ${message.messageId}] from: ${message?.from}, notification: [${message?.notification?.title}:${message?.notification?.body}]")
        message.data.let { map ->
            DebugLogUtil.i("PUSH: $map")
            val peerId = map["oppositeUserId"] ?: ""
            map["type"]?.let { type ->
                when (type) {
                    PushNotificationData.TYPE_LIKE -> BusEvent.PushNewLike(peerId)
                    PushNotificationData.TYPE_MATCH -> BusEvent.PushNewMatch(peerId)
                    PushNotificationData.TYPE_MESSAGE -> BusEvent.PushNewMessage(peerId)
                    else -> null
                }
            }
            ?.let { Bus.post(it) }
        }
    }

    @Suppress("CheckResult")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val params = Params().put(PushTokenEssenceUnauthorized(token))
                             .put("dontWarn", true)
        (application as? IBaseRingoidApplication)?.updatePushTokenUseCase
            ?.source(params = params)
            ?.breadcrumb("Update push token from bg Service", "token" to token)
            ?.subscribe({ DebugLogUtil.i("Successfully uploaded Firebase push token: $token") }, DebugLogUtil::e)
    }
}
