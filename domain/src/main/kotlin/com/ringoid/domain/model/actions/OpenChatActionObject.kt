package com.ringoid.domain.model.actions

import com.google.gson.annotations.SerializedName

class OpenChatActionObject(
    @SerializedName(COLUMN_OPEN_CHAT_COUNT) val count: Int,
    @SerializedName(COLUMN_OPEN_CHAT_TIME_SEC) val timeInSeconds: Int,
    actionTime: Long, actionType: String, sourceFeed: String, targetImageId: String, targetUserId: String)
    : ActionObject(actionTime = actionTime, actionType = actionType, sourceFeed = sourceFeed,
    targetImageId = targetImageId, targetUserId = targetUserId) {

    companion object {
        const val COLUMN_OPEN_CHAT_COUNT = "openChatCount"
        const val COLUMN_OPEN_CHAT_TIME_SEC = "openChatTimeSec"
    }
}
