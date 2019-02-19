package com.ringoid.data.remote.model.image

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.domain.model.Mappable
import com.ringoid.domain.model.image.Image

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
    @Expose @SerializedName(COLUMN_CLIENT_IMAGE_ID) val clientImageId: String,
    @Expose @SerializedName(COLUMN_ORIGIN_IMAGE_ID) val originImageId: String,
    @Expose @SerializedName(COLUMN_IMAGE_URI) val imageUri: String?,
    errorCode: String = "", errorMessage: String = "", repeatAfterSec: Long = 0L)
    : BaseResponse(errorCode, errorMessage, repeatAfterSec), Mappable<Image> {

    companion object {
        const val COLUMN_CLIENT_IMAGE_ID = "clientPhotoId"
        const val COLUMN_ORIGIN_IMAGE_ID = "originPhotoId"
        const val COLUMN_IMAGE_URI = "uri"
    }

    override fun map(): Image = Image(id = originImageId, uri = imageUri)

    override fun toString(): String =
        "ImageUploadUrlResponse(clientImageId='$clientImageId', originImageId='$originImageId', imageUri='$imageUri', ${super.toString()})"
}
