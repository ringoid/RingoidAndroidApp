package com.ringoid.data.remote.model.image.essence

import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence

/**
 * {
 *  "accessToken":"adasdasd-fadfs-sdffd",
 *  "photoId":"lsdkfjlskdjf-sdflksndfl"
 * }
 */
data class ImageDeleteEssence(
    @SerializedName(COLUMN_ACCESS_TOKEN) val accessToken: String,
    @SerializedName(COLUMN_IMAGE_ID) val imageId: String) : IEssence {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_IMAGE_ID = "photoId"
    }
}
