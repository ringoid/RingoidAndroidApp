package com.ringoid.data.local.model.image

import androidx.room.*
import com.ringoid.data.local.model.feed.ProfileDbo

@Entity(tableName = BaseImageDbo.TABLE_NAME, indices = [Index(value = [ProfileDbo.COLUMN_ID], name = "baseImageDboIndex")],
        foreignKeys = [ForeignKey(entity = ProfileDbo::class, parentColumns = [ProfileDbo.COLUMN_ID],
                                  childColumns = [BaseImageDbo.COLUMN_PROFILE_ID],
                                  onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)])
data class BaseImageDbo(
    @PrimaryKey @ColumnInfo(name = COLUMN_ID) val id: Int,
    @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: String,
    @ColumnInfo(name = COLUMN_URL) val url: String) {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_PROFILE_ID = "profileId"
        const val COLUMN_URL = "url"

        const val TABLE_NAME = "Images"
    }
}
