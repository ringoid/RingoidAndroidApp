package com.ringoid.domain.model.essence.image

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * {
 *  "accessToken":"adasdasd-fadfs-sdffd",
 *  "photoId":"lsdkfjlskdjf-sdflksndfl"
 * }
 */
data class ImageDeleteEssence(
    @Expose @SerializedName(COLUMN_ACCESS_TOKEN) val accessToken: String,
    @Expose @SerializedName(COLUMN_IMAGE_ID) override val imageId: String) : IImageDeleteEssence {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_IMAGE_ID = "photoId"

        fun from(essence: ImageDeleteEssenceUnauthorized, accessToken: String): ImageDeleteEssence =
            ImageDeleteEssence(accessToken = accessToken, imageId = essence.imageId)
    }

    fun copyWith(imageId: String): ImageDeleteEssence = ImageDeleteEssence(accessToken, imageId)
}
