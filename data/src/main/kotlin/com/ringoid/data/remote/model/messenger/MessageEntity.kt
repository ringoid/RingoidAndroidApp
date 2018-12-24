package com.ringoid.data.remote.model.messenger

import com.google.gson.annotations.SerializedName

/**
 * {
 *   "wasYouSender":false,
 *   "text":"Hi"
 * }
 */
data class MessageEntity(
    @SerializedName(COLUMN_FLAG_CURRENT_USER) val isCurrentUser: Boolean,
    @SerializedName(COLUMN_TEXT) val text: String) {

    companion object {
        const val COLUMN_FLAG_CURRENT_USER = "wasYouSender"
        const val COLUMN_TEXT = "text"
    }
}
