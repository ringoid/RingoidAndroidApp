package com.ringoid.data.remote.model.image

import com.google.gson.annotations.Expose
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
 *     {"photoId":"12dd","originPhotoId":"sldkjflkjlkjlkjf","photoUri":"https://bla-bla.com/sss.jpg","likes":22,"blocked":true},
 *     {"photoId":"13dd","originPhotoId":"mnbmvnbcxlsdfhwo","photoUri":"https://bla-bla.com/sss2.jpg","likes":0,"blocked":false}
 *   ]
 * }
 */
class UserImageListResponse(
    @Expose @SerializedName(COLUMN_IMAGES) val images: List<UserImageEntity> = emptyList(),
    errorCode: String = "", errorMessage: String = "", repeatRequestAfter: Long = 0L)
    : BaseResponse(errorCode, errorMessage, repeatRequestAfter), Mappable<List<UserImage>> {

    companion object {
        const val COLUMN_IMAGES = "photos"
    }

    fun copyWith(images: List<UserImageEntity>): UserImageListResponse =
        UserImageListResponse(images = images, errorCode = errorCode, errorMessage = errorMessage, repeatRequestAfter = repeatRequestAfter)

    override fun map(): List<UserImage> = images.mapList()

    override fun toString(): String = "UserImageListResponse(images=${images.joinToString(", ", "[", "]")}, ${super.toString()})"
}
