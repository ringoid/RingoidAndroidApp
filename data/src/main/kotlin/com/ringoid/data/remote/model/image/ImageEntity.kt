package com.ringoid.data.remote.model.image

import com.google.gson.annotations.SerializedName

/**
 * {
 *   "photoId":"12dd",
 *   "photoUri":"https://bla-bla.com/sss.jpg"
 * }
 */
open class ImageEntity(
    @SerializedName(COLUMN_ID) val id: String,
    @SerializedName(COLUMN_URI) val uri: String) {

    companion object {
        const val COLUMN_ID = "photoId"
        const val COLUMN_URI = "photoUri"
    }
}
