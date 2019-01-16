package com.ringoid.domain.model.actions

import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.DelayFromLast
import com.ringoid.domain.action_storage.VIEW_DELAY_ON_TRIGGER

class ViewActionObject(
    @SerializedName(COLUMN_VIEW_COUNT) val count: Int = 1,
    @SerializedName(COLUMN_VIEW_TIME_SEC) val timeInSeconds: Int,
    actionTime: Long, actionType: String, sourceFeed: String, targetImageId: String, targetUserId: String)
    : ActionObject(actionTime = actionTime, actionType = actionType, sourceFeed = sourceFeed,
                   targetImageId = targetImageId, targetUserId = targetUserId,
                   triggerStrategies = listOf(DelayFromLast(VIEW_DELAY_ON_TRIGGER))) {

    companion object {
        const val COLUMN_VIEW_COUNT = "viewCount"
        const val COLUMN_VIEW_TIME_SEC = "viewTimeSec"
    }
}
