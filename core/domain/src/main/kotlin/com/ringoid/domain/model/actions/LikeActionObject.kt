package com.ringoid.domain.model.actions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.Immediate
import com.ringoid.utility.randomInt

class LikeActionObject(
    @Expose @SerializedName(COLUMN_LIKE_COUNT) val likeCount: Int = 1,
    id: Int = randomInt(),
    actionTime: Long = System.currentTimeMillis(),
    sourceFeed: String,
    targetImageId: String, targetUserId: String)
    : ActionObject(
        id = id,
        actionTime = actionTime, actionType = ACTION_TYPE_LIKE,
        sourceFeed = sourceFeed,
        targetImageId = targetImageId, targetUserId = targetUserId,
        triggerStrategies = listOf(Immediate)) {

    companion object {
        const val COLUMN_LIKE_COUNT = "likeCount"
    }

    override fun propertyString(): String? = "likeCount=$likeCount"
}
