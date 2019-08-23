package com.ringoid.domain.model.essence.image

/**
 * Used to construct [ImageDeleteEssence] later assigning access token retrieved from data layer.
 */
data class ImageDeleteEssenceUnauthorized(override val imageId: String) : IImageDeleteEssence
