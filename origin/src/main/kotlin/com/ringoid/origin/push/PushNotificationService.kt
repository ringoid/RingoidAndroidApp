package com.ringoid.origin.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.push.UpdatePushTokenUseCase
import com.ringoid.domain.model.essence.push.PushTokenEssenceUnauthorized
import timber.log.Timber

class PushNotificationService(private val updatePushTokenUseCase: UpdatePushTokenUseCase)
    : FirebaseMessagingService() {

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
                    PushType.MESSAGE -> PushUtils.createNotification(applicationContext, title = it["text"])
                    PushType.SYSTEM -> {}
                    else -> {}
                }
            } else {
                Timber.v("PUSH: Payload data is empty in push notification")
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val params = Params().put(PushTokenEssenceUnauthorized(token))
        updatePushTokenUseCase.source(params = params)
            .subscribe({ DebugLogUtil.i("Successfully uploaded Firebase push token: $token") }, Timber::e)
    }
}
