package com.ringoid.data.local.database.model.feed.property

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = UserMessagedFeedItemIdDbo.TABLE_NAME)
data class UserMessagedFeedItemIdDbo(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) val id: Int = 0,
    @ColumnInfo(name = COLUMN_FEED_ITEM_ID) val feedItemId: String) {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_FEED_ITEM_ID = "feedItemId"

        const val TABLE_NAME = "UserMessagedFeedItemIds"
    }
}
