package com.ringoid.domain.repository.push

import com.ringoid.domain.model.essence.push.PushTokenEssenceUnauthorized
import io.reactivex.Completable

interface IPushRepository {

    fun updatePushToken(essence: PushTokenEssenceUnauthorized): Completable
}
