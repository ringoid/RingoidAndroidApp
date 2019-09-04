package com.ringoid.data.local.database.model.action_storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ringoid.domain.model.Mappable
import com.ringoid.domain.model.actions.*
import com.ringoid.utility.ValueUtils

@Entity(tableName = ActionObjectDbo.TABLE_NAME)
data class ActionObjectDbo(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) val id: Int = 0,
    @ColumnInfo(name = COLUMN_ACTION_TIME) val actionTime: Long,
    @ColumnInfo(name = COLUMN_ACTION_TYPE) val actionType: String,
    @ColumnInfo(name = COLUMN_USED) val used: Int = 0,
    // advanced properties
    @ColumnInfo(name = COLUMN_SOURCE_FEED) var sourceFeed: String = "",
    @ColumnInfo(name = COLUMN_TARGET_IMAGE_ID) var targetImageId: String = "",
    @ColumnInfo(name = COLUMN_TARGET_USER_ID) var targetUserId: String = "",
    // concrete action objects
    @ColumnInfo(name = COLUMN_NUMBER_BLOCK_REASON) var blockReasonNumber: Int = 0,
    @ColumnInfo(name = COLUMN_LOCATION_LATITUDE) var latitude: Double = 0.0,
    @ColumnInfo(name = COLUMN_LOCATION_LONGITUDE) var longitude: Double = 0.0,
    @ColumnInfo(name = COLUMN_MESSAGE_CLIENT_ID) var messageClientId: String = "",
    @ColumnInfo(name = COLUMN_MESSAGE_TEXT) var messageText: String = "",
    @ColumnInfo(name = COLUMN_OPEN_CHAT_TIME_MILLIS) var openChatTimeMillis: Long = 0L,
    @ColumnInfo(name = COLUMN_VIEW_CHAT_TIME_MILLIS) var viewChatTimeMillis: Long = 0L,
    @ColumnInfo(name = COLUMN_VIEW_TIME_MILLIS) var viewTimeMillis: Long = 0L) : Mappable<OriginActionObject> {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_ACTION_TIME = "actionTime"
        const val COLUMN_ACTION_TYPE = "actionType"
        const val COLUMN_USED = "used"

        // advanced properties
        const val COLUMN_SOURCE_FEED = "sourceFeed"
        const val COLUMN_TARGET_IMAGE_ID = "targetPhotoId"
        const val COLUMN_TARGET_USER_ID = "targetUserId"

        // concrete action objects
        const val COLUMN_NUMBER_BLOCK_REASON = "blockReasonNum"
        const val COLUMN_LOCATION_LATITUDE = "latitude"
        const val COLUMN_LOCATION_LONGITUDE = "longitude"
        const val COLUMN_MESSAGE_CLIENT_ID = "messageClientId"
        const val COLUMN_MESSAGE_TEXT = "messageText"
        const val COLUMN_OPEN_CHAT_TIME_MILLIS = "openChatTimeMillis"
        const val COLUMN_VIEW_CHAT_TIME_MILLIS = "viewChatTimeMillis"
        const val COLUMN_VIEW_TIME_MILLIS = "viewTimeMillis"

        const val TABLE_NAME = "ActionObjects"

        fun from(aobj: OriginActionObject): ActionObjectDbo =
            ActionObjectDbo(actionTime = aobj.actionTime, actionType = aobj.actionType)
    }

    internal fun isValid(): Boolean =
        when (actionType) {
            ActionObject.ACTION_TYPE_LOCATION -> ValueUtils.isValidLocation(latitude = latitude, longitude = longitude)
            else -> true
        }

    override fun map(): OriginActionObject =
        when (actionType) {
            ActionObject.ACTION_TYPE_BLOCK -> BlockActionObject(numberOfBlockReason = blockReasonNumber, actionTime = actionTime, sourceFeed = sourceFeed, targetImageId = targetImageId, targetUserId = targetUserId)
            ActionObject.ACTION_TYPE_DEBUG -> DebugActionObject()
            ActionObject.ACTION_TYPE_LOCATION -> LocationActionObject(latitude = latitude, longitude = longitude, actionTime = actionTime)
            ActionObject.ACTION_TYPE_LIKE -> LikeActionObject(actionTime = actionTime, sourceFeed = sourceFeed, targetImageId = targetImageId, targetUserId = targetUserId)
            ActionObject.ACTION_TYPE_MESSAGE -> MessageActionObject(clientId = messageClientId, text = messageText, actionTime = actionTime, sourceFeed = sourceFeed, targetImageId = targetImageId, targetUserId = targetUserId)
            ActionObject.ACTION_TYPE_OPEN_CHAT -> OpenChatActionObject(timeInMillis = openChatTimeMillis, actionTime = actionTime, sourceFeed = sourceFeed, targetImageId = targetImageId, targetUserId = targetUserId)
            ActionObject.ACTION_TYPE_UNLIKE -> UnlikeActionObject(actionTime = actionTime, sourceFeed = sourceFeed, targetImageId = targetImageId, targetUserId = targetUserId)
            ActionObject.ACTION_TYPE_VIEW -> ViewActionObject(timeInMillis = viewTimeMillis, actionTime = actionTime, sourceFeed = sourceFeed, targetImageId = targetImageId, targetUserId = targetUserId)
            ActionObject.ACTION_TYPE_VIEW_CHAT -> ViewChatActionObject(timeInMillis = viewChatTimeMillis, actionTime = actionTime, sourceFeed = sourceFeed, targetImageId = targetImageId, targetUserId = targetUserId)
            else -> throw IllegalArgumentException("Unsupported action type: $actionType")
        }
}
