package com.ringoid.data.remote.model.feed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.domain.DomainUtil
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
    @Expose @SerializedName(COLUMN_TOTAL_NOT_FILTERED_LIKES) val totalNotFilteredLikes: Int = DomainUtil.BAD_VALUE,
    @Expose @SerializedName(COLUMN_TOTAL_NOT_FILTERED_MESSAGES) val totalNotFilteredMessages: Int = DomainUtil.BAD_VALUE,
    errorCode: String = "", errorMessage: String = "", repeatAfterSec: Long = 0L)
    : BaseResponse(errorCode, errorMessage, repeatAfterSec), Mappable<Lmm> {

    fun copyWith(likes: List<FeedItemEntity> = this.likes, matches: List<FeedItemEntity> = this.matches,
                 messages: List<FeedItemEntity> = this.messages): LmmResponse =
        LmmResponse(likes = likes, matches = matches, messages = messages,
                    totalNotFilteredLikes = totalNotFilteredLikes,
                    totalNotFilteredMessages = totalNotFilteredMessages,
                    errorCode = errorCode, errorMessage = errorMessage)

    companion object {
        const val COLUMN_LIKES = "likesYou"
        const val COLUMN_MATCHES = "matches"
        const val COLUMN_MESSAGES = "messages"
        const val COLUMN_TOTAL_NOT_FILTERED_LIKES = "allLikesYouProfilesNum"
        const val COLUMN_TOTAL_NOT_FILTERED_MESSAGES = "allMessagesProfilesNum"
    }

    override fun map(): Lmm = Lmm(likes = likes.mapList(), matches = matches.mapList(), messages = messages.mapList(),
                                  totalNotFilteredLikes = totalNotFilteredLikes,
                                  totalNotFilteredMessages = totalNotFilteredMessages)

    override fun toLogString(): String = "likes[${likes.size}], matches[${matches.size}], messages=[${messages.size}], " +
                                         "tl=$totalNotFilteredLikes, tm=$totalNotFilteredMessages"

    override fun toString(): String =
        "LmmResponse(likes=${likes.joinToString(", ", "[", "]")}, " +
                    "matches=${matches.joinToString(", ", "[", "]")}, " +
                    "messages=${messages.joinToString(", ", "[", "]")}, " +
                    "totalNotFilteredLikes=$totalNotFilteredLikes, totalNotFilteredMessages=$totalNotFilteredMessages, " +
                    "${super.toString()})"
}
