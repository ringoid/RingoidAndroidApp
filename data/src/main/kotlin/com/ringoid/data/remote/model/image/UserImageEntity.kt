package com.ringoid.data.remote.model.image

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.image.UserImage

/**
 * {
 *   "photoId":"12dd",
 *   "originPhotoId":"sldkjflkjlkjlkjf",
 *   "photoUri":"https://bla-bla.com/sss.jpg",
 *   "likes":22
 * }
 */
class UserImageEntity(
    @Expose @SerializedName(COLUMN_ORIGIN_ID) val originId: String,
    @Expose @SerializedName(COLUMN_NUMBER_LIKES) val numberOfLikes: Int,
    id: String, uri: String) : BaseImageEntity<UserImage>(id = id, uri = uri) {

    companion object {
        const val COLUMN_ORIGIN_ID = "originPhotoId"
        const val COLUMN_NUMBER_LIKES = "likes"
    }

    override fun map(): UserImage = UserImage(originId = originId, numberOfLikes = numberOfLikes, id = id, uri = uri)
}
