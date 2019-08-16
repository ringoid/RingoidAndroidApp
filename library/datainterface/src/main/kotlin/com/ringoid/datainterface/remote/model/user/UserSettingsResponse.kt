package com.ringoid.datainterface.remote.model.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.datainterface.remote.model.BaseResponse

/**
 * {
 *   "errorCode":"",
 *   "errorMessage":"",
 *   "whoCanSeePhoto":"OPPOSITE",
 *   "safeDistanceInMeter":0,
 *   "pushMessages":true,
 *   "pushMatches":true,
 *   "pushLikes":"EVERY"
 * }
 */
class UserSettingsResponse(
    @Expose @SerializedName(COLUMN_PUSH_LIKES) val pushLikes: Boolean = false,
    @Expose @SerializedName(COLUMN_PUSH_MATCHES) val pushMatches: Boolean = false,
    @Expose @SerializedName(COLUMN_PUSH_MESSAGES) val pushMessages: Boolean = false,
    @Expose @SerializedName(COLUMN_PUSH_SAFE_DISTANCE) val safeDistance: Int = 0,
    @Expose @SerializedName(COLUMN_PUSH_WHO_CAN_SEE_PHOTO) val whoCanSeePhoto: String = "",
    errorCode: String = "", errorMessage: String = "", repeatAfterSec: Long = 0L)
    : BaseResponse(errorCode, errorMessage, repeatAfterSec) {

    companion object {
        const val COLUMN_PUSH_LIKES = "pushLikes"
        const val COLUMN_PUSH_MATCHES = "pushMatches"
        const val COLUMN_PUSH_MESSAGES = "pushMessages"
        const val COLUMN_PUSH_SAFE_DISTANCE = "safeDistanceInMeter"
        const val COLUMN_PUSH_WHO_CAN_SEE_PHOTO = "whoCanSeePhoto"
    }

    override fun toString(): String =
        "UserSettingsResponse(pushLikes=$pushLikes, pushMatches=$pushMatches, pushMessages=$pushMessages, " +
                "safeDistance=$safeDistance, whoCanSeePhoto='$whoCanSeePhoto', ${super.toString()})"
}
