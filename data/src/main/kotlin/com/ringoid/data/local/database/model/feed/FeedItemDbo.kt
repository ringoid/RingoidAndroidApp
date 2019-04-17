package com.ringoid.data.local.database.model.feed

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ringoid.domain.model.Mappable
import com.ringoid.domain.model.feed.FeedItem

@Entity(tableName = FeedItemDbo.TABLE_NAME)
data class FeedItemDbo(
    @PrimaryKey @ColumnInfo(name = COLUMN_ID, index = true) val id: String,
    @ColumnInfo(name = COLUMN_FLAG_NOT_SEEN) val isNotSeen: Boolean) {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_FLAG_NOT_SEEN = "notSeen"

        const val TABLE_NAME = "FeedItems"

        fun from(feedItem: FeedItem): FeedItemDbo =
            FeedItemDbo(id = feedItem.id, isNotSeen = feedItem.isNotSeen)
    }
}
