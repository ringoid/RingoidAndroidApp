package com.ringoid.datainterface.remote.model.messenger

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * {
 *   "msgAt":12894399,
 *   "msgId":"sdkjfh-12j",
 *   "clientMsgId":"sadf112",
 *   "text":"Hi",
 *   "haveBeenRead":true,
 *   "wasYouSender":false
 * }
 */
data class MessageEntity(
    @Expose @SerializedName(COLUMN_ID) val id: String,
    @Expose @SerializedName(COLUMN_CLIENT_ID) val clientId: String,
    @Expose @SerializedName(COLUMN_FLAG_CURRENT_USER) val isCurrentUser: Boolean,
    @Expose @SerializedName(COLUMN_FLAG_READ_BY_PEER) val isReadByPeer: Boolean,
    @Expose @SerializedName(COLUMN_TEXT) val text: String,
    @Expose @SerializedName(COLUMN_TIMESTAMP) val ts: Long) {

    companion object {
        const val COLUMN_ID = "msgId"
        const val COLUMN_CLIENT_ID = "clientMsgId"
        const val COLUMN_FLAG_CURRENT_USER = "wasYouSender"
        const val COLUMN_FLAG_READ_BY_PEER = "haveBeenRead"
        const val COLUMN_TEXT = "text"
        const val COLUMN_TIMESTAMP = "msgAt"
    }

    override fun toString(): String = "MessageEntity(id='$id', clientId='$clientId', isCurrentUser=$isCurrentUser, haveBeenRead=$isReadByPeer, text='$text', ts=$ts)"
}
