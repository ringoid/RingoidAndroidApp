package com.ringoid.domain.model.actions

import com.google.gson.annotations.SerializedName

class ViewActionObject(
    @SerializedName(COLUMN_VIEW_COUNT) val count: Int,
    @SerializedName(COLUMN_VIEW_TIME_SEC) val timeInSeconds: Int,
    actionTime: Long, actionType: String, sourceFeed: String, targetImageId: String, targetUserId: String)
    : ActionObject(actionTime = actionTime, actionType = actionType, sourceFeed = sourceFeed,
                   targetImageId = targetImageId, targetUserId = targetUserId) {

    companion object {
        const val COLUMN_VIEW_COUNT = "viewCount"
        const val COLUMN_VIEW_TIME_SEC = "viewTimeSec"
    }
}
