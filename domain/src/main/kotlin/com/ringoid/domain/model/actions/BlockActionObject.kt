package com.ringoid.domain.model.actions

import com.google.gson.annotations.SerializedName

class BlockActionObject(
    @SerializedName(COLUMN_NUMBER_BLOCK_REASON) val numberOfBlockReson: Int,
    actionTime: Long, actionType: String, sourceFeed: String, targetImageId: String, targetUserId: String)
    : ActionObject(actionTime = actionTime, actionType = actionType, sourceFeed = sourceFeed,
                   targetImageId = targetImageId, targetUserId = targetUserId) {

    companion object {
        const val COLUMN_NUMBER_BLOCK_REASON = "blockReasonNum"
    }
}
