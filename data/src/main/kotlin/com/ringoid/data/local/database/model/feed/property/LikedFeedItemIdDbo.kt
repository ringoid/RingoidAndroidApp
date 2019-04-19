package com.ringoid.data.local.database.model.feed.property

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = LikedFeedItemIdDbo.TABLE_NAME)
data class LikedFeedItemIdDbo(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) val id: Int = 0,
    @ColumnInfo(name = COLUMN_FEED_ITEM_ID) val feedItemId: String,
    @ColumnInfo(name = COLUMN_IMAGE_ID) val imageId: String) {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_FEED_ITEM_ID = "feedItemId"
        const val COLUMN_IMAGE_ID = "imageId"

        const val TABLE_NAME = "LikedFeedItemIds"
    }
}
