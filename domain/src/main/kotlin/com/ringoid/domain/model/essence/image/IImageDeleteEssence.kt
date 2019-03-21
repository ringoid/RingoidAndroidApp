package com.ringoid.domain.model.essence.image

import com.ringoid.domain.model.IEssence

interface IImageDeleteEssence : IEssence {

    val imageId: String

    override fun toSentryPayload(): String = "[imageId=$imageId]"
}
