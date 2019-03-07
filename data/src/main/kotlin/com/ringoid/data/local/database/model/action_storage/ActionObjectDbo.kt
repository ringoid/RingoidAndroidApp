package com.ringoid.data.local.database.model.action_storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = ActionObjectDbo.TABLE_NAME)
data class ActionObjectDbo(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) val id: Int = 0,
    @ColumnInfo(name = COLUMN_ACTION_TIME) val actionTime: Long,
    @ColumnInfo(name = COLUMN_ACTION_TYPE) val actionType: String,
    @ColumnInfo(name = COLUMN_SOURCE_FEED) val sourceFeed: String,
    @ColumnInfo(name = COLUMN_TARGET_IMAGE_ID) val targetImageId: String,
    @ColumnInfo(name = COLUMN_TARGET_USER_ID) val targetUserId: String,
    // concrete action objects
    @ColumnInfo(name = COLUMN_NUMBER_BLOCK_REASON) val blockReasonNumber: Int,
    @ColumnInfo(name = COLUMN_MESSAGE_TEXT) val messageText: String,
    @ColumnInfo(name = COLUMN_OPEN_CHAT_TIME_MILLIS) val openChatTimeMillis: Long,
    @ColumnInfo(name = COLUMN_VIEW_TIME_MILLIS) val viewTimeMillis: Long) {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_ACTION_TIME = "actionTime"
        const val COLUMN_ACTION_TYPE = "actionType"
        const val COLUMN_SOURCE_FEED = "sourceFeed"
        const val COLUMN_TARGET_IMAGE_ID = "targetPhotoId"
        const val COLUMN_TARGET_USER_ID = "targetUserId"

        // concrete action objects
        const val COLUMN_NUMBER_BLOCK_REASON = "blockReasonNum"
        const val COLUMN_MESSAGE_TEXT = "messageText"
        const val COLUMN_OPEN_CHAT_TIME_MILLIS = "openChatTimeMillis"
        const val COLUMN_VIEW_TIME_MILLIS = "viewTimeMillis"

        const val TABLE_NAME = "ActionObjects"
    }
}
