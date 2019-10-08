package com.ringoid.domain.model.actions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.actions.ActionObject.Companion.ACTION_TYPE_MESSAGE_READ
import com.ringoid.utility.randomInt

class ReadMessageActionObject(
    @Expose @SerializedName(COLUMN_MESSAGE_ID) val messageId: String,
    @Expose @SerializedName(COLUMN_PEER_ID) val peerId: String,
    id: Int = randomInt(), actionTime: Long = System.currentTimeMillis())
    : OriginActionObject(id = id, actionTime = actionTime, actionType = ACTION_TYPE_MESSAGE_READ) {

    companion object {
        const val COLUMN_MESSAGE_ID = "msgId"
        const val COLUMN_PEER_ID = "userId"
    }

    override fun propertyString(): String? = "messageId=$messageId,peerId=$peerId"
}
