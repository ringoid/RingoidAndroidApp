package com.ringoid.domain.model.actions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.action_storage.TriggerStrategy
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.model.IEssence

sealed class BaseActionObject

/**
 * {
 *   "actionType":"LOCATION",
 *   "actionTime":12342342354  // unix time
 * }
 */
open class OriginActionObject(
    @Expose @SerializedName(ActionObject.COLUMN_ACTION_TIME) val actionTime: Long = System.currentTimeMillis(),
    @Expose @SerializedName(ActionObject.COLUMN_ACTION_TYPE) val actionType: String,
    val triggerStrategies: List<TriggerStrategy> = emptyList())
    : BaseActionObject(), IEssence {

    protected open fun propertyString(): String? = null

    override fun toString(): String {
        val property = propertyString().takeIf { !it.isNullOrBlank() }?.let { "$it, " } ?: ""
        return "${javaClass.simpleName}(${property}actionTime=$actionTime, actionType='$actionType', triggerStrategies=$triggerStrategies)"
    }

    open fun toActionString(): String = "$actionType(${propertyString()},aT=${actionTime % 1000000})"

    @DebugOnly override fun toDebugPayload(): String = toActionString()
    override fun toSentryPayload(): String = toActionString()
}

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
    actionTime: Long = System.currentTimeMillis(), actionType: String,
    @Expose @SerializedName(COLUMN_SOURCE_FEED) val sourceFeed: String,
    @Expose @SerializedName(COLUMN_TARGET_IMAGE_ID) val targetImageId: String,
    @Expose @SerializedName(COLUMN_TARGET_USER_ID) val targetUserId: String,
    triggerStrategies: List<TriggerStrategy> = emptyList())
    : OriginActionObject(actionTime, actionType, triggerStrategies) {

    fun key(): Pair<String, String> = targetImageId to targetUserId

    companion object {
        const val COLUMN_ACTION_TIME = "actionTime"
        const val COLUMN_ACTION_TYPE = "actionType"
        const val COLUMN_SOURCE_FEED = "sourceFeed"
        const val COLUMN_TARGET_IMAGE_ID = "targetPhotoId"
        const val COLUMN_TARGET_USER_ID = "targetUserId"

        const val ACTION_TYPE_BLOCK = "BLOCK"
        const val ACTION_TYPE_LIKE = "LIKE"
        const val ACTION_TYPE_LOCATION = "LOCATION"
        const val ACTION_TYPE_MESSAGE = "MESSAGE"
        const val ACTION_TYPE_OPEN_CHAT = "OPEN_CHAT"
        const val ACTION_TYPE_UNLIKE = "UNLIKE"
        const val ACTION_TYPE_VIEW = "VIEW"
        const val ACTION_TYPE_VIEW_CHAT = "VIEW_CHAT"
    }

    override fun toString(): String {
        val property = propertyString().takeIf { !it.isNullOrBlank() }?.let { "$it, " } ?: ""
        return "${javaClass.simpleName}(${property}actionTime=$actionTime, actionType='$actionType', sourceFeed='$sourceFeed', targetImageId='$targetImageId', targetUserId='$targetUserId', triggerStrategies=$triggerStrategies)"
    }

    override fun toActionString(): String = "$actionType(${propertyString()},${targetIdsStr()})"
    protected fun targetIdsStr(): String =
        if (BuildConfig.IS_STAGING) {
            "i=${(targetImageId.indexOf('_')
                    .takeIf { it != -1 }
                    ?.let { targetImageId.substring(it + 1) }
                    ?: targetImageId)
                .substring(0..3)}," +
            "p=${targetUserId.substring(0..3)}," +
            "aT=${actionTime % 1000000},$sourceFeed"
        } else ""
}
