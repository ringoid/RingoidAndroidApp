package com.ringoid.domain.model.actions

import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.Immediate

class BlockActionObject(
    @SerializedName(COLUMN_NUMBER_BLOCK_REASON) val numberOfBlockReason: Int,
    actionTime: Long, sourceFeed: String, targetImageId: String, targetUserId: String)
    : ActionObject(actionTime = actionTime, actionType = "BLOCK", sourceFeed = sourceFeed,
                   targetImageId = targetImageId, targetUserId = targetUserId,
                   triggerStrategies = listOf(Immediate)) {

    companion object {
        const val COLUMN_NUMBER_BLOCK_REASON = "blockReasonNum"
    }
}
