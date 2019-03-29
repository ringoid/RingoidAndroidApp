package com.ringoid.domain.model.essence.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence

/**
 * {
 *   "accessToken":"adasdasd-fadfs-sdffd",
 *   "locale":"en",
 *   "push":true,
 *   "timeZone":3
 * }
 */
data class UpdateUserSettingsEssence(
    @Expose @SerializedName(COLUMN_ACCESS_TOKEN) val accessToken: String,
    @Expose @SerializedName(COLUMN_LOCALE) val locale: String? = null,
    @Expose @SerializedName(COLUMN_PUSH) val push: Boolean? = null,
    @Expose @SerializedName(COLUMN_TIMEZONE) val timeZone: Int? = null) : IEssence {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_LOCALE = "locale"
        const val COLUMN_PUSH = "push"
        const val COLUMN_TIMEZONE = "timeZone"

        fun from(essence: UpdateUserSettingsEssenceUnauthorized, accessToken: String): UpdateUserSettingsEssence =
            UpdateUserSettingsEssence(accessToken = accessToken, locale = essence.userSettings.locale,
                                      push = essence.userSettings.push, timeZone = essence.userSettings.timeZone)
    }
}
