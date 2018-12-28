package com.ringoid.data.local.database.model.image

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.ringoid.data.local.database.model.feed.ProfileDbo

@Entity(tableName = ImageDbo.TABLE_NAME,
        foreignKeys = [ForeignKey(entity = ProfileDbo::class, parentColumns = [ProfileDbo.COLUMN_ID],
                                  childColumns = [ImageDbo.COLUMN_PROFILE_ID],
                                  onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)])
class ImageDbo(
    @ColumnInfo(name = COLUMN_PROFILE_ID, index = true) val profileId: String,
    id: String, uri: String) : BaseImageDbo(id = id, uri = uri) {

    companion object {
        const val COLUMN_PROFILE_ID = "profileId"

        const val TABLE_NAME = "Images"
    }
}
