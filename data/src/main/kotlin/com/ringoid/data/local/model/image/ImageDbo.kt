package com.ringoid.data.local.model.image

import androidx.room.*
import com.ringoid.data.local.model.feed.ProfileDbo

@Entity(tableName = ImageDbo.TABLE_NAME, indices = [Index(value = [ProfileDbo.COLUMN_ID], name = "baseImageDboIndex")],
        foreignKeys = [ForeignKey(entity = ProfileDbo::class, parentColumns = [ProfileDbo.COLUMN_ID],
                                  childColumns = [ImageDbo.COLUMN_PROFILE_ID],
                                  onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)])
data class ImageDbo(
    @PrimaryKey @ColumnInfo(name = COLUMN_ID) val id: Int,
    @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: String,
    @ColumnInfo(name = COLUMN_URI) val uri: String) {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_PROFILE_ID = "profileId"
        const val COLUMN_URI = "uri"

        const val TABLE_NAME = "Images"
    }
}
