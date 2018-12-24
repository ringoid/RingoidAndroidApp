package com.ringoid.data.remote.model.image.essence

import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence

/**
 * {
 *   "accessToken":"adasdasd-fadfs-sdffd",
 *   "extension":"jpg",
 *   "clientPhotoId":"sldfjlskdfj--;lfk;lf"
 * }
 */
data class ImageUploadUrlEssence(
    @SerializedName(COLUMN_ACCESS_TOKEN) val accessToken: String,
    @SerializedName(COLUMN_CLIENT_IMAGE_ID) val clientImageId: String,
    @SerializedName(COLUMN_EXTENSION) val extension: String) : IEssence {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_CLIENT_IMAGE_ID = "clientPhotoId"
        const val COLUMN_EXTENSION = "extension"
    }
}
