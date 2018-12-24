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
class ImageEntity(
    @SerializedName(COLUMN_ORIGIN_ID) val originId: String,
    @SerializedName(COLUMN_NUMBER_LIKES) val numberOfLikes: Int,
    id: String, uri: String) : BaseImageEntity(id = id, uri = uri) {

    companion object {
        const val COLUMN_ORIGIN_ID = "originPhotoId"
        const val COLUMN_NUMBER_LIKES = "likes"
    }
}
