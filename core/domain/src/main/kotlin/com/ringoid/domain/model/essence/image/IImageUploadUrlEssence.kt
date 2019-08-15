package com.ringoid.domain.model.essence.image

import com.ringoid.domain.model.IEssence

interface IImageUploadUrlEssence : IEssence {

    val clientImageId: String
    val extension: String

    override fun toSentryPayload(): String = "[clientPhotoId=$clientImageId], extension=$extension]"
}
