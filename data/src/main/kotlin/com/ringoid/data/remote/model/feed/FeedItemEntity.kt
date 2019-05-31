package com.ringoid.data.remote.model.feed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.data.local.database.model.feed.FeedItemDbo.Companion.COLUMN_FLAG_NOT_SEEN
import com.ringoid.data.remote.model.feed.LmmResponse.Companion.COLUMN_MESSAGES
import com.ringoid.data.remote.model.image.ImageEntity
import com.ringoid.data.remote.model.messenger.MessageEntity
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.misc.Gender
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
 *   ],
 *   "age": 37,
 *   "sex": "male",
 *   "property": 0,
 *   "transport": 0,
 *   "education": 0,
 *   "income": 0,
 *   "height": 0,
 *   "hairColor": 0
 * }
 */
open class FeedItemEntity(
    id: String,
    sortPosition: Int,
    distanceText: String? = null,
    images: List<ImageEntity> = emptyList(),
    @Expose @SerializedName(COLUMN_MESSAGES) val messages: List<MessageEntity> = emptyList(),
    @Expose @SerializedName(COLUMN_FLAG_NOT_SEEN) val isNotSeen: Boolean,
    lastOnlineStatus: String? = null,
    lastOnlineText: String? = null,
    age: Int,
    education: Int,
    gender: String?,
    hairColor: Int,
    height: Int,
    income: Int,
    property: Int,
    transport: Int)
    : BaseProfileEntity<FeedItem>(
        id = id,
        sortPosition = sortPosition,
        distanceText = distanceText,
        images = images,
        lastOnlineStatus = lastOnlineStatus,
        lastOnlineText = lastOnlineText,
        age = age,
        education = education,
        gender = gender,
        hairColor = hairColor,
        height = height,
        income = income,
        property = property,
        transport = transport) {

    companion object {
        const val COLUMN_FLAG_NOT_SEEN = "notSeen"
        const val COLUMN_MESSAGES = "messages"
    }

    override fun map(): FeedItem =
        FeedItem(
            id = id,
            distanceText = distanceText,
            images = images.mapList(),
            messages = messages.mapIndexed { index, message ->
                val peerId = id.takeIf { !message.isCurrentUser } ?: DomainUtil.CURRENT_USER_ID
                Message(id = "${id}_$index", chatId = id, peerId = peerId, text = message.text)
            }.toMutableList(),
            lastOnlineStatus = lastOnlineStatus,
            lastOnlineText = lastOnlineText,
            isNotSeen = isNotSeen,
            age = age,
            education = education,
            gender = Gender.from(gender),
            hairColor = hairColor,
            height = height,
            income = income,
            property = property,
            transport = transport)

    override fun toString(): String = "FeedItemEntity(isNotSeen=$isNotSeen, messages=${messages.joinToString(", ", "[", "]")}, ${super.toString()})"
}
