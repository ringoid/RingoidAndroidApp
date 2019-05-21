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
    @ColumnInfo(name = COLUMN_SORT_POSITION) val sortPosition: Int = DomainUtil.BAD_SORT_POSITION,
    @ColumnInfo(name = COLUMN_URI_LOCAL) val uriLocal: String? = null,
    id: String, uri: String?, thumbnailUri: String? = null)
    : BaseImageDbo(id = id, uri = uri, thumbnailUri = thumbnailUri), Mappable<UserImage> {

    companion object {
        const val COLUMN_FLAG_BLOCKED = "blocked"
        const val COLUMN_ORIGIN_ID = "originPhotoId"
        const val COLUMN_NUMBER_LIKES = "likes"
        const val COLUMN_SORT_POSITION = "sortPosition"
        const val COLUMN_URI_LOCAL = "uriLocal"

        const val TABLE_NAME = "UserImages"

        fun from(image: UserImage): UserImageDbo =
            UserImageDbo(originId = image.originId, numberOfLikes = image.numberOfLikes, isBlocked = image.isBlocked,
                         sortPosition = image.sortPosition, uriLocal = image.uriLocal,
                         id = image.id, uri = image.uri, thumbnailUri = image.thumbnailUri)
    }

    fun copyWith(originId: String = this.originId, numberOfLikes: Int = this.numberOfLikes, isBlocked: Boolean = this.isBlocked,
                 sortPosition: Int = this.sortPosition, uriLocal: String? = this.uriLocal, uri: String? = this.uri,
                 thumbnailUri: String? = this.thumbnailUri)
        : UserImageDbo = UserImageDbo(originId = originId, numberOfLikes = numberOfLikes, isBlocked = isBlocked,
                                      sortPosition = sortPosition, uriLocal = uriLocal,
                                      id = id, uri = uri, thumbnailUri = thumbnailUri)

    override fun map(): UserImage = UserImage(originId = originId, numberOfLikes = numberOfLikes, isBlocked = isBlocked,
                                              sortPosition = sortPosition, uriLocal = uriLocal,
                                              id = id, uri = uri, thumbnailUri = thumbnailUri)
}
