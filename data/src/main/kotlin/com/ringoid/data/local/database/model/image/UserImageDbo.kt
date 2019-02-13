package com.ringoid.data.local.database.model.image

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.Mappable
import com.ringoid.domain.model.image.UserImage

@Entity(tableName = UserImageDbo.TABLE_NAME)
class UserImageDbo(
    @ColumnInfo(name = COLUMN_ORIGIN_ID) val originId: String = DomainUtil.BAD_ID,
    @ColumnInfo(name = COLUMN_NUMBER_LIKES) val numberOfLikes: Int = 0,
    @ColumnInfo(name = COLUMN_FLAG_BLOCKED) val isBlocked: Boolean = false,
    @ColumnInfo(name = COLUMN_SORT_POSITION) val sortPosition: Int = DomainUtil.BAD_POSITION,
    id: String, uri: String?) : BaseImageDbo(id = id, uri = uri), Mappable<UserImage> {

    companion object {
        const val COLUMN_FLAG_BLOCKED = "blocked"
        const val COLUMN_ORIGIN_ID = "originPhotoId"
        const val COLUMN_NUMBER_LIKES = "likes"
        const val COLUMN_SORT_POSITION = "sortPosition"

        const val TABLE_NAME = "UserImages"

        fun from(image: UserImage): UserImageDbo =
            UserImageDbo(originId = image.originId, numberOfLikes = image.numberOfLikes, isBlocked = image.isBlocked,
                         sortPosition = image.sortPosition, id = image.id, uri = image.uri)
    }

    override fun map(): UserImage = UserImage(originId = originId, numberOfLikes = numberOfLikes, isBlocked = isBlocked,
                                              sortPosition = sortPosition, id = id, uri = uri)
}
