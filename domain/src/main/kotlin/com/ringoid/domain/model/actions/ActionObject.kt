package com.ringoid.domain.model.actions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.TriggerStrategy
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.model.IEssence

sealed class BaseActionObject

/**
 * {
 *   "sourceFeed":"new_faces",  // who_liked_me, matches, messages
 *   "actionType":"BLOCK",
 *   "targetUserId":"skdfkjhkjsdhf",
 *   "targetPhotoId":"sldfnlskdj",
 *   "actionTime":12342342354  // unix time
 * }
 */
open class ActionObject(
    @Expose @SerializedName(COLUMN_ACTION_TIME) val actionTime: Long = System.currentTimeMillis(),
    @Expose @SerializedName(COLUMN_ACTION_TYPE) val actionType: String,
    @Expose @SerializedName(COLUMN_SOURCE_FEED) val sourceFeed: String,
    @Expose @SerializedName(COLUMN_TARGET_IMAGE_ID) val targetImageId: String,
    @Expose @SerializedName(COLUMN_TARGET_USER_ID) val targetUserId: String,
    val triggerStrategies: List<TriggerStrategy> = emptyList())
    : BaseActionObject(), IEssence {

    companion object {
        const val COLUMN_ACTION_TIME = "actionTime"
        const val COLUMN_ACTION_TYPE = "actionType"
        const val COLUMN_SOURCE_FEED = "sourceFeed"
        const val COLUMN_TARGET_IMAGE_ID = "targetPhotoId"
        const val COLUMN_TARGET_USER_ID = "targetUserId"

        const val ACTION_TYPE_BLOCK = "BLOCK"
        const val ACTION_TYPE_LIKE = "LIKE"
        const val ACTION_TYPE_MESSAGE = "MESSAGE"
        const val ACTION_TYPE_OPEN_CHAT = "OPEN_CHAT"
        const val ACTION_TYPE_UNLIKE = "UNLIKE"
        const val ACTION_TYPE_VIEW = "VIEW"
        const val ACTION_TYPE_VIEW_CHAT = "VIEW_CHAT"
    }

    protected open fun propertyString(): String? = null

    override fun toString(): String {
        val property = propertyString().takeIf { !it.isNullOrBlank() }?.let { "$it, " } ?: ""
        return "${javaClass.simpleName}(${property}actionTime=$actionTime, actionType='$actionType', sourceFeed='$sourceFeed', targetImageId='$targetImageId', targetUserId='$targetUserId', triggerStrategies=$triggerStrategies)"
    }

    open fun toActionString(): String = "ACTION"

    @DebugOnly override fun toDebugPayload(): String = toActionString()
    override fun toSentryPayload(): String = "[${javaClass.simpleName}]"
}
