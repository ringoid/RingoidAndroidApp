package com.ringoid.data.remote.model.user.essence

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
    @SerializedName(COLUMN_ACCESS_TOKEN) val accessToken: String,
    @SerializedName(COLUMN_PUSH_LIKES) val pushLikes: Boolean,
    @SerializedName(COLUMN_PUSH_MATCHES) val pushMatches: Boolean,
    @SerializedName(COLUMN_PUSH_MESSAGES) val pushMessages: Boolean,
    @SerializedName(COLUMN_PUSH_SAFE_DISTANCE) val safeDistance: Int) : IEssence {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_PUSH_LIKES = "pushLikes"
        const val COLUMN_PUSH_MATCHES = "pushMatches"
        const val COLUMN_PUSH_MESSAGES = "pushMessages"
        const val COLUMN_PUSH_SAFE_DISTANCE = "safeDistanceInMeter"
    }
}
