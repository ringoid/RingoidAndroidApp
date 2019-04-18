package com.ringoid.data.local.database.model.feed.property

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = UserMessagedFeedItemIdDbo.TABLE_NAME)
data class UserMessagedFeedItemIdDbo(@PrimaryKey @ColumnInfo(name = COLUMN_ID) val id: String) {

    companion object {
        const val COLUMN_ID = "id"

        const val TABLE_NAME = "UserMessagedFeedItemIds"
    }
}
