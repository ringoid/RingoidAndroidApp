package com.ringoid.data.remote.model.messenger

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * {
 *   "wasYouSender":false,
 *   "text":"Hi"
 * }
 */
data class MessageEntity(
    @Expose @SerializedName(COLUMN_FLAG_CURRENT_USER) val isCurrentUser: Boolean,
    @Expose @SerializedName(COLUMN_TEXT) val text: String) {

    companion object {
        const val COLUMN_FLAG_CURRENT_USER = "wasYouSender"
        const val COLUMN_TEXT = "text"
    }
}
