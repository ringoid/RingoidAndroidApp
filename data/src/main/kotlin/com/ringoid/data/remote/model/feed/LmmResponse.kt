package com.ringoid.data.remote.model.feed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.BaseResponse

/**
 * {
 *   "errorCode":"",
 *   "errorMessage":"",
 *   "repeatRequestAfterSec":0,
 *   "likesYou": [...],
 *   "matches": [...],
 *   "messages": [...]
 * }
 */
class LmmResponse(
    @Expose @SerializedName(COLUMN_LIKES) val likes: List<FeedItemEntity> = emptyList(),
    @Expose @SerializedName(COLUMN_MATCHES) val matches: List<FeedItemEntity> = emptyList(),
    @Expose @SerializedName(COLUMN_MESSAGES) val messages: List<FeedItemEntity> = emptyList(),
    @Expose @SerializedName(COLUMN_REPEAT_AFTER_SEC) val repeatAfterSec: Int,
    errorCode: String = "", errorMessage: String = "") : BaseResponse(errorCode, errorMessage) {

    companion object {
        const val COLUMN_LIKES = "likesYou"
        const val COLUMN_MATCHES = "matches"
        const val COLUMN_MESSAGES = "messages"
        const val COLUMN_REPEAT_AFTER_SEC = "repeatRequestAfterSec"
    }
}
