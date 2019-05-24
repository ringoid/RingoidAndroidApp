package com.ringoid.data.local.database.model.feed

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ringoid.data.remote.model.feed.BaseProfileEntity.Companion.COLUMN_DISTANCE_TEXT
import com.ringoid.data.remote.model.feed.BaseProfileEntity.Companion.COLUMN_LAST_ONLINE_STATUS
import com.ringoid.data.remote.model.feed.BaseProfileEntity.Companion.COLUMN_LAST_ONLINE_TEXT
import com.ringoid.data.remote.model.feed.FeedItemEntity.Companion.COLUMN_FLAG_NOT_SEEN
import com.ringoid.domain.model.feed.FeedItem

@Entity(tableName = FeedItemDbo.TABLE_NAME)
data class FeedItemDbo(
    @PrimaryKey @ColumnInfo(name = COLUMN_ID, index = true) val id: String,
    @ColumnInfo(name = COLUMN_DISTANCE_TEXT) val distanceText: String?,
    @ColumnInfo(name = COLUMN_FLAG_NOT_SEEN) val isNotSeen: Boolean,
    @ColumnInfo(name = COLUMN_LAST_ONLINE_STATUS) val lastOnlineStatus: String?,
    @ColumnInfo(name = COLUMN_LAST_ONLINE_TEXT) val lastOnlineText: String?,
    @ColumnInfo(name = COLUMN_SOURCE_FEED) val sourceFeed: String) {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_DISTANCE_TEXT = "distanceText"
        const val COLUMN_FLAG_NOT_SEEN = "notSeen"
        const val COLUMN_LAST_ONLINE_STATUS = "lastOnlineFlag"
        const val COLUMN_LAST_ONLINE_TEXT = "lastOnlineText"
        const val COLUMN_SOURCE_FEED = "sourceFeed"

        const val TABLE_NAME = "FeedItems"

        fun from(feedItem: FeedItem, sourceFeed: String): FeedItemDbo =
            FeedItemDbo(id = feedItem.id, distanceText = feedItem.distanceText, isNotSeen = feedItem.isNotSeen,
                lastOnlineStatus = feedItem.lastOnlineStatus, lastOnlineText = feedItem.lastOnlineText, sourceFeed = sourceFeed)
    }
}
