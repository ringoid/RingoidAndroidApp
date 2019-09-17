package com.ringoid.domain.model.actions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.Immediate
import com.ringoid.domain.model.actions.ActionObject.Companion.ACTION_TYPE_MESSAGE_READ

class ReadMessageActionObject(
    @Expose @SerializedName(COLUMN_MESSAGE_ID) val messageId: String,
    @Expose @SerializedName(COLUMN_PEER_ID) val peerId: String,
    actionTime: Long = System.currentTimeMillis())
    : OriginActionObject(actionTime = actionTime, actionType = ACTION_TYPE_MESSAGE_READ,
                         triggerStrategies = listOf(Immediate)) {

    companion object {
        const val COLUMN_MESSAGE_ID = "msgId"
        const val COLUMN_PEER_ID = "userId"
    }

    override fun propertyString(): String? = "messageId=$messageId,peerId=$peerId"
}