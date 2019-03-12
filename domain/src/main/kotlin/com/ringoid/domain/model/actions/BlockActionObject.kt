package com.ringoid.domain.model.actions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.Immediate

class BlockActionObject(
    @Expose @SerializedName(COLUMN_NUMBER_BLOCK_REASON) val numberOfBlockReason: Int,
    actionTime: Long = System.currentTimeMillis(),
    sourceFeed: String, targetImageId: String, targetUserId: String)
    : ActionObject(actionTime = actionTime, actionType = ACTION_TYPE_BLOCK, sourceFeed = sourceFeed,
                   targetImageId = targetImageId, targetUserId = targetUserId,
                   triggerStrategies = listOf(Immediate)) {

    companion object {
        const val COLUMN_NUMBER_BLOCK_REASON = "blockReasonNum"
    }

    override fun propertyString(): String? = "blockReason=$numberOfBlockReason"

    override fun toActionString(): String = "BLOCK($numberOfBlockReason,${targetIdsStr()})"
}
