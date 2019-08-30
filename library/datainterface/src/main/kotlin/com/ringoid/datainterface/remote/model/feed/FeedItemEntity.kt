package com.ringoid.datainterface.remote.model.feed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.datainterface.remote.model.image.ImageEntity
import com.ringoid.datainterface.remote.model.messenger.MessageEntity
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
 *   "hairColor": 0,
 *   "children":0,
 *
 *   "name":"Mikhail",
 *   "jobTitle":"Developer",
 *   "company":"Ringoid",
 *   "education":"BGTU Voenmeh",
 *   "about":"Nice person",
 *   "instagram":"unknown",
 *   "tikTok":"unknown",
 *   "whereLive":"St.Petersburg",
 *   "whereFrom":"Leningrad"
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
    age: Int = DomainUtil.UNKNOWN_VALUE,
    children: Int = DomainUtil.UNKNOWN_VALUE,
    education: Int = DomainUtil.UNKNOWN_VALUE,
    gender: String? = null,
    hairColor: Int = DomainUtil.UNKNOWN_VALUE,
    height: Int = DomainUtil.UNKNOWN_VALUE,
    income: Int = DomainUtil.UNKNOWN_VALUE,
    property: Int = DomainUtil.UNKNOWN_VALUE,
    transport: Int = DomainUtil.UNKNOWN_VALUE,
    about: String? = null,
    company: String? = null,
    jobTitle: String? = null,
    name: String? = null,
    instagram: String? = null,
    tiktok: String? = null,
    status: String? = null,
    university: String? = null,
    whereFrom: String? = null,
    whereLive: String? = null)
    : BaseProfileEntity<FeedItem>(
        id = id,
        sortPosition = sortPosition,
        distanceText = distanceText,
        images = images,
        lastOnlineStatus = lastOnlineStatus,
        lastOnlineText = lastOnlineText,
        age = age,
        children = children,
        education = education,
        gender = gender,
        hairColor = hairColor,
        height = height,
        income = income,
        property = property,
        transport = transport,
        about = about,
        company = company,
        jobTitle = jobTitle,
        name = name,
        instagram = instagram,
        tiktok = tiktok,
        status = status,
        university = university,
        whereFrom = whereFrom,
        whereLive = whereLive) {

    companion object {
        const val COLUMN_FLAG_NOT_SEEN = "notSeen"
        const val COLUMN_MESSAGES = "messages"
    }

    override fun map(): FeedItem =
        FeedItem(
            id = id,
            distanceText = distanceText,
            images = images.mapList(),
            messages = messages.map { message ->
                val peerId = id.takeIf { !message.isCurrentUser } ?: DomainUtil.CURRENT_USER_ID
                Message(
                    id = message.id,
                    chatId = id,
                    clientId = message.clientId,
                    peerId = peerId,
                    text = message.text,
                    ts = message.ts)
            }.toMutableList(),
            lastOnlineStatus = lastOnlineStatus,
            lastOnlineText = lastOnlineText,
            isNotSeen = isNotSeen,
            age = age,
            children = children,
            education = education,
            gender = Gender.from(gender),
            hairColor = hairColor,
            height = height,
            income = income,
            property = property,
            transport = transport,
            about = about,
            company = company,
            jobTitle = jobTitle,
            name = name,
            instagram = instagram,
            tiktok = tiktok,
            status = status,
            university = university,
            whereFrom = whereFrom,
            whereLive = whereLive)

    override fun toString(): String = "FeedItemEntity(isNotSeen=$isNotSeen, messages=${messages.joinToString(", ", "[", "]")}, ${super.toString()})"
}
