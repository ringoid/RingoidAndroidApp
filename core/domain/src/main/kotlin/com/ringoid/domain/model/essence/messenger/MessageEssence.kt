package com.ringoid.domain.model.essence.messenger

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence
import com.ringoid.domain.model.essence.action.ActionObjectEssence

data class MessageEssence(
    @Expose @SerializedName(COLUMN_PEER_ID) val peerId: String,
    @Expose @SerializedName(COLUMN_TEXT) val text: String = "",
    val aObjEssence: ActionObjectEssence? = null) : IEssence {

    companion object {
        const val COLUMN_PEER_ID = "profileId"
        const val COLUMN_TEXT = "text"
    }

    override fun toSentryPayload(): String = "[peerId=$peerId, text=$text]"
}
