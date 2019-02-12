package com.ringoid.domain.model.essence.image

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence

/**
 * {
 *  "accessToken":"adasdasd-fadfs-sdffd",
 *  "photoId":"lsdkfjlskdjf-sdflksndfl"
 * }
 */
data class ImageDeleteEssence(
    @Expose @SerializedName(COLUMN_ACCESS_TOKEN) val accessToken: String,
    @Expose @SerializedName(COLUMN_IMAGE_ID) val imageId: String) : IEssence {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_IMAGE_ID = "photoId"
    }

    fun copyWith(imageId: String): ImageDeleteEssence = ImageDeleteEssence(accessToken, imageId)

    override fun toSentryPayload(): String = "[photoId=$imageId]"
}
