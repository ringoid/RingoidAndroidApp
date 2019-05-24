package com.ringoid.data.remote.model.feed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.image.ImageEntity
import com.ringoid.data.remote.model.messenger.MessageEntity
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.mapList
import com.ringoid.domain.model.messenger.Message

/**
 * {
 *   "userId":"9091127b2a88b002fad4ef55beb0264222c1ebb7",
 *   "defaultSortingOrderPosition":0,
 *   "notSeen":true,
 *   "messages":[...],
 *   "photos": [
 *     {
 *       "photoId": "480x640_sfsdfsdfsdf",
 *       "photoUri": "https://bla-bla.jpg"
 *     },
 *     ...
 *   ]
 * }
 */
class FeedItemEntity(
    @Expose @SerializedName(COLUMN_FLAG_NOT_SEEN) val isNotSeen: Boolean,
    @Expose @SerializedName(COLUMN_MESSAGES) val messages: List<MessageEntity> = emptyList(),
    id: String, sortPosition: Int, images: List<ImageEntity> = emptyList())
    : BaseProfileEntity<FeedItem>(id = id, sortPosition = sortPosition, images = images) {

    companion object {
        const val COLUMN_FLAG_NOT_SEEN = "notSeen"
        const val COLUMN_MESSAGES = "messages"
    }

    override fun map(): FeedItem =
        FeedItem(id = id, distanceText = distanceText, isNotSeen = isNotSeen,
                 images = images.mapList(),
                 messages = messages.mapIndexed { index, message ->
                     val peerId = id.takeIf { !message.isCurrentUser } ?: DomainUtil.CURRENT_USER_ID
                     Message(id = "${id}_$index", chatId = id, peerId = peerId, text = message.text)
                 }.toMutableList(),
                 lastOnlineStatus = lastOnlineStatus,
                 lastOnlineText = lastOnlineText)

    override fun toString(): String =
        "FeedItemEntity(isNotSeen=$isNotSeen, messages=${messages.joinToString(", ", "[", "]")}, ${super.toString()})"
}
