package com.ringoid.domain.model.essence.user

import com.ringoid.domain.model.IEssence

interface IReferralCodeEssence : IEssence {

    val referralId: String

    override fun toSentryPayload(): String = "[referralId=$referralId]"
}
