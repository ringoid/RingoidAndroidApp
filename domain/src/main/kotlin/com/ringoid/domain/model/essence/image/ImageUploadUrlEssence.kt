package com.ringoid.domain.model.essence.image

import com.google.gson.annotations.SerializedName
import com.ringoid.utility.randomString

/**
 * {
 *   "accessToken":"adasdasd-fadfs-sdffd",
 *   "extension":"jpg",
 *   "clientPhotoId":"sldfjlskdfj--;lfk;lf"
 * }
 */
data class ImageUploadUrlEssence(
    @SerializedName(COLUMN_ACCESS_TOKEN) val accessToken: String,
    @SerializedName(COLUMN_CLIENT_IMAGE_ID) override val clientImageId: String = randomString(),
    @SerializedName(COLUMN_EXTENSION) override val extension: String) : IImageUploadUrlEssence {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_CLIENT_IMAGE_ID = "clientPhotoId"
        const val COLUMN_EXTENSION = "extension"

        fun from(essence: ImageUploadUrlEssenceUnauthorized, accessToken: String): ImageUploadUrlEssence =
            ImageUploadUrlEssence(accessToken = accessToken, clientImageId = essence.clientImageId, extension = essence.extension)
    }
}
