package com.ringoid.data.remote.model.image

import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.BaseResponse

/**
 * {
 *   "errorCode":"",
 *   "errorMessage":"",
 *   "photos":[
 *     {"photoId":"12dd","originPhotoId":"sldkjflkjlkjlkjf","photoUri":"https://bla-bla.com/sss.jpg","likes":22},
 *     {"photoId":"13dd","originPhotoId":"mnbmvnbcxlsdfhwo","photoUri":"https://bla-bla.com/sss2.jpg","likes":0}
 *   ]
 * }
 */
class ImageListResponse(
    @SerializedName(COLUMN_IMAGES) val images: List<ImageEntity> = emptyList(),
    errorCode: String = "", errorMessage: String = "") : BaseResponse(errorCode, errorMessage) {

    companion object {
        const val COLUMN_IMAGES = "photos"
    }
}
