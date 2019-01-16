package com.ringoid.domain.model.actions

import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.Immediate

class MessageActionObject(
    @SerializedName(COLUMN_TEXT) val text: String,
    actionTime: Long, sourceFeed: String, targetImageId: String, targetUserId: String)
    : ActionObject(actionTime = actionTime, actionType = "MESSAGE", sourceFeed = sourceFeed,
                   targetImageId = targetImageId, targetUserId = targetUserId,
                   triggerStrategies = listOf(Immediate)) {

    companion object {
        const val COLUMN_TEXT = "text"
    }
}
