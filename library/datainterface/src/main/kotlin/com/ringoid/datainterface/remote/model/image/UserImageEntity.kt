package com.ringoid.datainterface.remote.model.image

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.image.IImage
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
    @Expose @SerializedName(COLUMN_FLAG_BLOCKED) val isBlocked: Boolean,
    id: String, uri: String, thumbnailUri: String? = null)
    : BaseImageEntity<UserImage>(id = id, uri = uri, thumbnailUri = thumbnailUri) {

    companion object {
        const val COLUMN_FLAG_BLOCKED = "blocked"
        const val COLUMN_ORIGIN_ID = "originPhotoId"
        const val COLUMN_NUMBER_LIKES = "likes"

        fun from(image: IImage): UserImageEntity =
            UserImageEntity(originId = DomainUtil.BAD_ID, numberOfLikes = 0, isBlocked = false, id = image.id, uri = image.uri ?: "")
    }

    override fun map(): UserImage = UserImage(originId = originId, numberOfLikes = numberOfLikes, isBlocked = isBlocked,
                                              id = id, uri = uri, thumbnailUri = thumbnailUri)

    override fun toString(): String =
        "UserImageEntity(originId='$originId', numberOfLikes=$numberOfLikes, isBlocked=$isBlocked, ${super.toString()})"
}
