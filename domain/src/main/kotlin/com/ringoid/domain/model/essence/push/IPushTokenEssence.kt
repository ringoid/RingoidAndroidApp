package com.ringoid.domain.model.essence.push

import com.ringoid.domain.model.IEssence

interface IPushTokenEssence : IEssence {

    val pushToken: String

    override fun toSentryPayload(): String = "[pushToken=$pushToken]"
}
