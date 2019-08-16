package com.ringoid.datainterface.remote.model.messenger

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * {
 *   "wasYouSender":false,
 *   "text":"Hi",
 *   "msgId":"sdkjfh-12j",
 *   "clientMsgId":"sadf112",
 *   "msgAt":12894399
 * }
 */
data class MessageEntity(
    @Expose @SerializedName(COLUMN_ID) val id: String,
    @Expose @SerializedName(COLUMN_CLIENT_ID) val clientId: String,
    @Expose @SerializedName(COLUMN_FLAG_CURRENT_USER) val isCurrentUser: Boolean,
    @Expose @SerializedName(COLUMN_TEXT) val text: String,
    @Expose @SerializedName(COLUMN_TIMESTAMP) val ts: Long) {

    companion object {
        const val COLUMN_ID = "msgId"
        const val COLUMN_CLIENT_ID = "clientMsgId"
        const val COLUMN_FLAG_CURRENT_USER = "wasYouSender"
        const val COLUMN_TEXT = "text"
        const val COLUMN_TIMESTAMP = "msgAt"
    }

    override fun toString(): String = "MessageEntity(id='$id', clientId='$clientId', isCurrentUser=$isCurrentUser, text='$text', ts=$ts)"
}
