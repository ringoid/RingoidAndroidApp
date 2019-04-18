package com.ringoid.data.local.database.model.feed.property

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = LikedFeedItemIdDbo.TABLE_NAME)
data class LikedFeedItemIdDbo(
    @PrimaryKey @ColumnInfo(name = COLUMN_ID) val id: String,
    @ColumnInfo(name = COLUMN_IMAGE_ID) val imageId: String) {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_IMAGE_ID = "imageId"

        const val TABLE_NAME = "LikedFeedItemIds"
    }
}
