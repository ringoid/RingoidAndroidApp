package com.ringoid.domain.misc

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence

data class PushSettingsRaw(
    @Expose @SerializedName(COLUMN_PUSH) var push: Boolean = true,
    @Expose @SerializedName(COLUMN_PUSH_LIKES) var pushLikes: Boolean = true,
    @Expose @SerializedName(COLUMN_PUSH_MATCHES) var pushMatches: Boolean = true,
    @Expose @SerializedName(COLUMN_PUSH_MESSAGES) var pushMessages: Boolean = true,
    @Expose @SerializedName(COLUMN_PUSH_VIBRATION) var pushVibration: Boolean = false) : IEssence {

    companion object {
        const val COLUMN_PUSH = "push"
        const val COLUMN_PUSH_LIKES = "pushNewLike"
        const val COLUMN_PUSH_MATCHES = "pushNewMatch"
        const val COLUMN_PUSH_MESSAGES = "pushNewMessage"
        const val COLUMN_PUSH_VIBRATION = "vibration"
    }
}
