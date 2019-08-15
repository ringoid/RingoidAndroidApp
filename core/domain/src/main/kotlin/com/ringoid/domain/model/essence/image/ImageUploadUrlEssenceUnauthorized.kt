package com.ringoid.domain.model.essence.image

import com.ringoid.utility.randomString

/**
 * Used to construct [ImageUploadUrlEssence] later assigning access token retrieved from data layer.
 */
data class ImageUploadUrlEssenceUnauthorized(
    override val clientImageId: String = randomString(),
    override val extension: String) : IImageUploadUrlEssence
