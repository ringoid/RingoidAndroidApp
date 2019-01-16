package com.ringoid.domain.model.actions

import com.google.gson.annotations.SerializedName

class OpenChatActionObject(
    @SerializedName(COLUMN_OPEN_CHAT_COUNT) val count: Int = 1,
    @SerializedName(COLUMN_OPEN_CHAT_TIME_SEC) val timeInSeconds: Int,
    actionTime: Long, sourceFeed: String, targetImageId: String, targetUserId: String)
    : ActionObject(actionTime = actionTime, actionType = "OPEN_CHAT", sourceFeed = sourceFeed,
    targetImageId = targetImageId, targetUserId = targetUserId) {

    companion object {
        const val COLUMN_OPEN_CHAT_COUNT = "openChatCount"
        const val COLUMN_OPEN_CHAT_TIME_SEC = "openChatTimeSec"
    }
}
