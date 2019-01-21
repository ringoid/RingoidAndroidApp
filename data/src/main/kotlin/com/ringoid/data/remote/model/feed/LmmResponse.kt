package com.ringoid.data.remote.model.feed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.domain.model.Mappable
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.domain.model.mapList

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
    @Expose @SerializedName(COLUMN_REPEAT_AFTER_SEC) val repeatAfterSec: Int = 0,
    errorCode: String = "", errorMessage: String = "")
    : BaseResponse(errorCode, errorMessage), Mappable<Lmm> {

    fun copyWith(likes: List<FeedItemEntity> = this.likes, matches: List<FeedItemEntity> = this.matches,
                 messages: List<FeedItemEntity> = this.messages): LmmResponse =
        LmmResponse(likes = likes, matches = matches, messages = messages, repeatAfterSec = repeatAfterSec,
                    errorCode = errorCode, errorMessage = errorMessage)

    companion object {
        const val COLUMN_LIKES = "likesYou"
        const val COLUMN_MATCHES = "matches"
        const val COLUMN_MESSAGES = "messages"
        const val COLUMN_REPEAT_AFTER_SEC = "repeatRequestAfterSec"
    }

    override fun map(): Lmm = Lmm(likes = likes.mapList(), matches = matches.mapList(), messages = messages.mapList())
}
