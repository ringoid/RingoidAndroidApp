package com.ringoid.data.local.database.model.image

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.ringoid.domain.model.Mappable
import com.ringoid.domain.model.image.UserImage

@Entity(tableName = UserImageDbo.TABLE_NAME)
class UserImageDbo(
    @ColumnInfo(name = COLUMN_ORIGIN_ID) val originId: String,
    @ColumnInfo(name = COLUMN_NUMBER_LIKES) val numberOfLikes: Int = 0,
    id: String, uri: String?) : BaseImageDbo(id = id, uri = uri), Mappable<UserImage> {

    companion object {
        const val COLUMN_ORIGIN_ID = "originPhotoId"
        const val COLUMN_NUMBER_LIKES = "likes"

        const val TABLE_NAME = "UserImages"

        fun from(image: UserImage): UserImageDbo =
            UserImageDbo(originId = image.originId, numberOfLikes = image.numberOfLikes, id = image.id, uri = image.uri)
    }

    override fun map(): UserImage = UserImage(originId = originId, numberOfLikes = numberOfLikes, id = id, uri = uri)
}
