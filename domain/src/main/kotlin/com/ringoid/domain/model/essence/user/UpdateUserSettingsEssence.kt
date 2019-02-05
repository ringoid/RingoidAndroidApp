package com.ringoid.domain.model.essence.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence

/**
 * {
 *   "accessToken":"adasdasd-fadfs-sdffd",
 *   "safeDistanceInMeter":0,
 *   "pushMessages":true,
 *   "pushMatches":true,
 *   "pushLikes":"EVERY" //possible values NONE/EVERY/10_NEW/100_NEW
 * }
 */
data class UpdateUserSettingsEssence(
    @Expose @SerializedName(COLUMN_ACCESS_TOKEN) val accessToken: String,
    @Expose @SerializedName(COLUMN_PUSH_LIKES) val pushLikes: Boolean,
    @Expose @SerializedName(COLUMN_PUSH_MATCHES) val pushMatches: Boolean,
    @Expose @SerializedName(COLUMN_PUSH_MESSAGES) val pushMessages: Boolean,
    @Expose @SerializedName(COLUMN_PUSH_SAFE_DISTANCE) val safeDistance: Int) : IEssence {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_PUSH_LIKES = "pushLikes"
        const val COLUMN_PUSH_MATCHES = "pushMatches"
        const val COLUMN_PUSH_MESSAGES = "pushMessages"
        const val COLUMN_PUSH_SAFE_DISTANCE = "safeDistanceInMeter"
    }

    override fun toSentryPayload(): String = "[pushLikes=$pushLikes, pushMatches=$pushMatches, pushMessages=$pushMessages, safeDistance=$safeDistance]"
}
