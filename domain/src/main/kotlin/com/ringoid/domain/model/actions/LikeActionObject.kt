package com.ringoid.domain.model.actions

import com.google.gson.annotations.SerializedName

class LikeActionObject(
    @SerializedName(COLUMN_LIKE_COUNT) val likeCount: Int,
    actionTime: Long, actionType: String, sourceFeed: String, targetImageId: String, targetUserId: String)
    : ActionObject(actionTime = actionTime, actionType = actionType, sourceFeed = sourceFeed,
                   targetImageId = targetImageId, targetUserId = targetUserId) {

    companion object {
        const val COLUMN_LIKE_COUNT = "likeCount"
    }
}
