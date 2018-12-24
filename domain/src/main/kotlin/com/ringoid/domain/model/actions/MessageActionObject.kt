package com.ringoid.domain.model.actions

import com.google.gson.annotations.SerializedName

class MessageActionObject(
    @SerializedName(COLUMN_TEXT) val text: String,
    actionTime: Long, actionType: String, sourceFeed: String, targetImageId: String, targetUserId: String)
    : ActionObject(actionTime = actionTime, actionType = actionType, sourceFeed = sourceFeed,
                   targetImageId = targetImageId, targetUserId = targetUserId) {

    companion object {
        const val COLUMN_TEXT = "text"
    }
}
