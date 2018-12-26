package com.ringoid.data.local.model.image

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = UserImageDbo.TABLE_NAME)
class UserImageDbo(
    @ColumnInfo(name = COLUMN_ORIGIN_ID) val originId: String,
    @ColumnInfo(name = COLUMN_NUMBER_LIKES) val numberOfLikes: Int,
    id: String, uri: String) : BaseImageDbo(id = id, uri = uri) {

    companion object {
        const val COLUMN_ORIGIN_ID = "originPhotoId"
        const val COLUMN_NUMBER_LIKES = "likes"

        const val TABLE_NAME = "UserImages"
    }
}
