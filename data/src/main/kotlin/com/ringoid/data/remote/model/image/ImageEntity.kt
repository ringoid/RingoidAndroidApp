package com.ringoid.data.remote.model.image

import com.google.gson.annotations.SerializedName

/**
 * {
 *   "photoId":"12dd",
 *   "originPhotoId":"sldkjflkjlkjlkjf",
 *   "photoUri":"https://bla-bla.com/sss.jpg",
 *   "likes":22
 * }
 */
data class ImageEntity(
    @SerializedName(COLUMN_ID) val id: String,
    @SerializedName(COLUMN_ORIGIN_ID) val originId: String,
    @SerializedName(COLUMN_URI) val uri: String,
    @SerializedName(COLUMN_NUMBER_LIKES) val numberOfLikes: Int) {

    companion object {
        const val COLUMN_ID = "photoId"
        const val COLUMN_ORIGIN_ID = "originPhotoId"
        const val COLUMN_URI = "photoUri"
        const val COLUMN_NUMBER_LIKES = "likes"
    }
}
