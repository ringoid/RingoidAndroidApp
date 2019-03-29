package com.ringoid.domain.model.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UserSettings(
    @Expose @SerializedName(COLUMN_LOCALE) val locale: String,
    @Expose @SerializedName(COLUMN_PUSH) val push: Boolean,
    @Expose @SerializedName(COLUMN_TIMEZONE) val timeZone: Int) {

    companion object {
        const val COLUMN_LOCALE = "locale"
        const val COLUMN_PUSH = "push"
        const val COLUMN_TIMEZONE = "timeZone"
    }
}
