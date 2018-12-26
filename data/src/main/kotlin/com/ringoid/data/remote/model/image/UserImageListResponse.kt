package com.ringoid.data.remote.model.image

import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.domain.model.Mappable
import com.ringoid.domain.model.image.UserImage
import com.ringoid.domain.model.mapList

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
class UserImageListResponse(
    @SerializedName(COLUMN_IMAGES) val images: List<UserImageEntity> = emptyList(),
    errorCode: String = "", errorMessage: String = "")
    : BaseResponse(errorCode, errorMessage), Mappable<List<UserImage>> {

    companion object {
        const val COLUMN_IMAGES = "photos"
    }

    override fun map(): List<UserImage> = images.mapList()
}
