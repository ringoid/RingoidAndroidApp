package com.ringoid.origin.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ringoid.base.IBaseRingoidApplication
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.log.breadcrumb
import com.ringoid.domain.model.essence.push.PushTokenEssenceUnauthorized
import com.ringoid.domain.model.push.PushNotification
import timber.log.Timber

class PushNotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage?) {
        super.onMessageReceived(message)
        Timber.d("PUSH: Received push notification [id: ${message?.messageId}] from: ${message?.from}, notification: [${message?.notification?.title}:${message?.notification?.body}]")
        message?.data
            ?.also { DebugLogUtil.i("PUSH: $it") }
            ?.let { it[PushNotification.COLUMN_MAIN] }
            ?.let { PushNotification.fromJson(it) }
            ?.let { PushUtils.createNotification(applicationContext, title = it.content.body?.title) }
            ?: run { Timber.v("PUSH: Payload data is empty in push notification") }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val params = Params().put(PushTokenEssenceUnauthorized(token))
        (application as? IBaseRingoidApplication)?.updatePushTokenUseCase
            ?.source(params = params)
            ?.breadcrumb("Update push token from bg Service", "token" to token)
            ?.subscribe({ DebugLogUtil.i("Successfully uploaded Firebase push token: $token") }, Timber::e)
    }
}
