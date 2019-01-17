package com.ringoid.domain.model.actions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class OpenChatActionObject(
    @Expose @SerializedName(COLUMN_OPEN_CHAT_COUNT) val count: Int = 1,
    @Expose @SerializedName(COLUMN_OPEN_CHAT_TIME_MILLIS) val timeInMillis: Long,
    actionTime: Long = System.currentTimeMillis(),
    sourceFeed: String, targetImageId: String, targetUserId: String)
    : ActionObject(actionTime = actionTime, actionType = "OPEN_CHAT", sourceFeed = sourceFeed,
    targetImageId = targetImageId, targetUserId = targetUserId) {

    companion object {
        const val COLUMN_OPEN_CHAT_COUNT = "openChatCount"
        const val COLUMN_OPEN_CHAT_TIME_MILLIS = "openChatTimeMillis"
    }
}
