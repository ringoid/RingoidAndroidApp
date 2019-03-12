package com.ringoid.domain.model.actions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.DelayFromLast
import com.ringoid.domain.action_storage.TriggerStrategy
import com.ringoid.domain.action_storage.VIEW_DELAY_ON_TRIGGER

class OpenChatActionObject(
    @Expose @SerializedName(COLUMN_OPEN_CHAT_COUNT) val count: Int = 1,
    @Expose @SerializedName(COLUMN_OPEN_CHAT_TIME_MILLIS) val timeInMillis: Long = 1,
    actionTime: Long = System.currentTimeMillis(),
    sourceFeed: String, targetImageId: String, targetUserId: String,
    triggerStrategies: List<TriggerStrategy> = listOf(DelayFromLast(VIEW_DELAY_ON_TRIGGER)))
    : ActionObject(actionTime = actionTime, actionType = ACTION_TYPE_OPEN_CHAT, sourceFeed = sourceFeed,
                   targetImageId = targetImageId, targetUserId = targetUserId,
                   triggerStrategies = triggerStrategies) {

    companion object {
        const val COLUMN_OPEN_CHAT_COUNT = "openChatCount"
        const val COLUMN_OPEN_CHAT_TIME_MILLIS = "openChatTimeMillis"
    }

    override fun propertyString(): String? = "count=$count, timeInMillis=$timeInMillis"

    override fun toActionString(): String = "OPEN_CHAT($timeInMillis)"
}
