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
    @Expose @SerializedName(COLUMN_PUSH_LIKES) val pushLikes: Boolean? = null,
    @Expose @SerializedName(COLUMN_PUSH_MATCHES) val pushMatches: Boolean? = null,
    @Expose @SerializedName(COLUMN_PUSH_MESSAGES) val pushMessages: Boolean? = null,
    @Expose @SerializedName(COLUMN_PUSH_VIBRATION) val pushVibration: Boolean? = null,
    @Expose @SerializedName(COLUMN_TIMEZONE) val timeZone: Int? = null) : IEssence {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_LOCALE = "locale"
        const val COLUMN_PUSH = "push"
        const val COLUMN_PUSH_LIKES = "pushNewLike"
        const val COLUMN_PUSH_MATCHES = "pushNewMatch"
        const val COLUMN_PUSH_MESSAGES = "pushNewMessage"
        const val COLUMN_PUSH_VIBRATION = "vibration"
        const val COLUMN_TIMEZONE = "timeZone"

        fun from(essence: UpdateUserSettingsEssenceUnauthorized, accessToken: String): UpdateUserSettingsEssence =
            UpdateUserSettingsEssence(
                accessToken = accessToken,
                locale = essence.userSettings.locale,
                push = essence.userSettings.push,
                pushLikes = essence.userSettings.pushLikes,
                pushMatches = essence.userSettings.pushMatches,
                pushMessages = essence.userSettings.pushMessages,
                pushVibration = essence.userSettings.pushVibration,
                timeZone = essence.userSettings.timeZone)
    }

    override fun toDebugPayload(): String = "[locale=$locale,push=$push,pushLikes=$pushLikes,pushMatches=$pushMatches," +
                                            "pushMessages=$pushMessages,pushVibration=$pushVibration,timezone=$timeZone]"
}
