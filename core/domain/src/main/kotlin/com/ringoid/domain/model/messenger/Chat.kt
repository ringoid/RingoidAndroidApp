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
    about: String? = null,
    company: String? = null,
    jobTitle: String? = null,
    name: String? = null,
    instagram: String? = null,
    tiktok: String? = null,
    status: String? = null,
    university: String? = null,
    whereFrom: String? = null,
    whereLive: String? = null,
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
        about = about,
        company = company,
        jobTitle = jobTitle,
        name = name,
        instagram = instagram,
        tiktok = tiktok,
        status = status,
        university = university,
        whereFrom = whereFrom,
        whereLive = whereLive,
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
            about = feedItem.about,
            company = feedItem.company,
            jobTitle = feedItem.jobTitle,
            name = feedItem.name,
            instagram = feedItem.instagram,
            tiktok = feedItem.tiktok,
            status = feedItem.status,
            university = feedItem.university,
            whereFrom = feedItem.whereFrom,
            whereLive = feedItem.whereLive,
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
            about = about,
            company = company,
            jobTitle = jobTitle,
            name = name,
            instagram = instagram,
            tiktok = tiktok,
            status = status,
            university = university,
            whereFrom = whereFrom,
            whereLive = whereLive,
            isNotSeen = isNotSeen,
            isRealModel = isRealModel)

    fun print(): String = "[${messages.size}]: ${messages.print(n = 5)} :: unconsumed: ${unconsumedSentLocalMessages.print()}"
}

val EmptyChat = Chat(EmptyFeedItem)
