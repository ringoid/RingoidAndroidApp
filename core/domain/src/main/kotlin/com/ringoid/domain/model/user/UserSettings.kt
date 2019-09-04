package com.ringoid.domain.model.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.misc.PushSettingsRaw

data class UserSettings(
    @Expose @SerializedName(COLUMN_LOCALE) val locale: String? = null,
    @Expose @SerializedName(COLUMN_PUSH) val push: Boolean? = null,
    @Expose @SerializedName(COLUMN_PUSH_LIKES) val pushLikes: Boolean? = null,
    @Expose @SerializedName(COLUMN_PUSH_MATCHES) val pushMatches: Boolean? = null,
    @Expose @SerializedName(COLUMN_PUSH_MESSAGES) val pushMessages: Boolean? = null,
    @Expose @SerializedName(COLUMN_PUSH_VIBRATION) val pushVibration: Boolean? = null,
    @Expose @SerializedName(COLUMN_TIMEZONE) val timeZone: Int? = null) {

    companion object {
        const val COLUMN_LOCALE = "locale"
        const val COLUMN_PUSH = "push"
        const val COLUMN_PUSH_LIKES = "pushNewLike"
        const val COLUMN_PUSH_MATCHES = "pushNewMatch"
        const val COLUMN_PUSH_MESSAGES = "pushNewMessage"
        const val COLUMN_PUSH_VIBRATION = "vibration"
        const val COLUMN_TIMEZONE = "timeZone"

        fun from(properties: PushSettingsRaw): UserSettings =
            UserSettings(
                push = properties.push,
                pushLikes = properties.pushLikes,
                pushMatches = properties.pushMatches,
                pushMessages = properties.pushMessages,
                pushVibration = properties.pushVibration)
    }
}
