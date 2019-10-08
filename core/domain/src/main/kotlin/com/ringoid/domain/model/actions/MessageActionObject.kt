package com.ringoid.domain.model.actions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.Immediate
import com.ringoid.utility.randomInt

class MessageActionObject(
    @Expose @SerializedName(COLUMN_CLIENT_ID) val clientId: String,
    @Expose @SerializedName(COLUMN_TEXT) val text: String,
    id: Int = randomInt(),
    actionTime: Long = System.currentTimeMillis(),
    sourceFeed: String,
    targetImageId: String, targetUserId: String)
    : ActionObject(
        id = id,
        actionTime = actionTime, actionType = ACTION_TYPE_MESSAGE,
        sourceFeed = sourceFeed,
        targetImageId = targetImageId, targetUserId = targetUserId,
        triggerStrategies = listOf(Immediate)) {

    companion object {
        const val COLUMN_CLIENT_ID = "clientMsgId"
        const val COLUMN_TEXT = "text"
    }

    override fun propertyString(): String? = "clientId=${clientId.substring(0..3)},text=$text"
}
