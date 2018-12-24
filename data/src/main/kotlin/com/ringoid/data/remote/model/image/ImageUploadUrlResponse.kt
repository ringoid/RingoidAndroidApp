package com.ringoid.data.remote.model.image

import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.BaseResponse

/**
 * {
 *   "errorCode":"",
 *   "errorMessage":"",
 *   "uri":"https://bla.com/bla",
 *   "originPhotoId":"sdljfhsljkdhgsdkj",
 *   "clientPhotoId":"sldfjlskdfj--;lfk;lf"
 * }
 */
class ImageUploadUrlResponse(
    @SerializedName(COLUMN_CLIENT_IMAGE_ID) val clientImageId: String,
    @SerializedName(COLUMN_ORIGIN_IMAGE_ID) val originImageId: String,
    @SerializedName(COLUMN_IMAGE_URI) val imageUri: String,
    errorCode: String = "", errorMessage: String = "") : BaseResponse(errorCode, errorMessage) {

    companion object {
        const val COLUMN_CLIENT_IMAGE_ID = "clientPhotoId"
        const val COLUMN_ORIGIN_IMAGE_ID = "originPhotoId"
        const val COLUMN_IMAGE_URI = "uri"
    }
}
