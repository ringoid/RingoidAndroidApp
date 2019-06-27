package com.ringoid.domain.model.messenger

import com.ringoid.domain.DomainUtil
import com.ringoid.domain.misc.Gender
import com.ringoid.domain.model.feed.EmptyFeedItem
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.image.IImage
import com.ringoid.domain.model.print

class Chat(
    id: String,
    distanceText: String? = null,
    images: List<IImage>,
    messages: List<Message>,
    lastOnlineStatus: String? = null,
    lastOnlineText: String? = null,
    age: Int = DomainUtil.UNKNOWN_VALUE,
    children: Int = DomainUtil.UNKNOWN_VALUE,
    education: Int = DomainUtil.UNKNOWN_VALUE,
    gender: Gender = Gender.UNKNOWN,
    hairColor: Int = DomainUtil.UNKNOWN_VALUE,
    height: Int = DomainUtil.UNKNOWN_VALUE,
    income: Int = DomainUtil.UNKNOWN_VALUE,
    property: Int = DomainUtil.UNKNOWN_VALUE,
    transport: Int = DomainUtil.UNKNOWN_VALUE,
    isNotSeen: Boolean,
    isRealModel: Boolean = true,
    val unconsumedSentLocalMessages: MutableList<Message> = mutableListOf())
    : FeedItem(
        id = id,
        distanceText = distanceText,
        images = images,
        messages = messages,
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
        isNotSeen = isNotSeen,
        isRealModel = isRealModel) {

    constructor(feedItem: FeedItem)
        : this(
            id = feedItem.id,
            distanceText = feedItem.distanceText,
            images = feedItem.images,
            messages = feedItem.messages,
            lastOnlineStatus = feedItem.lastOnlineStatus,
            lastOnlineText = feedItem.lastOnlineText,
            age = feedItem.age,
            children = feedItem.children,
            education = feedItem.education,
            gender = feedItem.gender,
            hairColor = feedItem.hairColor,
            height = feedItem.height,
            income = feedItem.income,
            property = feedItem.property,
            transport = feedItem.transport,
            isNotSeen = feedItem.isNotSeen,
            isRealModel = feedItem.isRealModel)

    fun copyWith(messages: List<Message>): Chat =
        Chat(
            id = id,
            distanceText = distanceText,
            images = images,
            messages = messages.toMutableList(),
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
            isNotSeen = isNotSeen,
            isRealModel = isRealModel)

    fun print(): String = "[${messages.size}]: ${messages.print(n = 5)} :: unconsumed: ${unconsumedSentLocalMessages.print()}"
}

val EmptyChat = Chat(EmptyFeedItem)
