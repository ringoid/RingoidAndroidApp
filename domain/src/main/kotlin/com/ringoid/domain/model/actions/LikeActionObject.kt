package com.ringoid.domain.model.actions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.Immediate

class LikeActionObject(
    @Expose @SerializedName(COLUMN_LIKE_COUNT) val likeCount: Int = 1,
    actionTime: Long = System.currentTimeMillis(),
    sourceFeed: String, targetImageId: String, targetUserId: String)
    : ActionObject(actionTime = actionTime, actionType = ACTION_TYPE_LIKE, sourceFeed = sourceFeed,
                   targetImageId = targetImageId, targetUserId = targetUserId,
                   triggerStrategies = listOf(Immediate)) {

    companion object {
        const val COLUMN_LIKE_COUNT = "likeCount"
    }

    override fun propertyString(): String? = "likeCount=$likeCount"
}
