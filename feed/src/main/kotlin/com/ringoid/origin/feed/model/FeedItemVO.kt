package com.ringoid.origin.feed.model

import com.ringoid.domain.DomainUtil
import com.ringoid.domain.misc.Gender
import com.ringoid.domain.model.feed.EmptyFeedItem
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.IFeedItem
import com.ringoid.domain.model.feed.Profile
import com.ringoid.domain.model.image.IImage
import com.ringoid.domain.model.messenger.Message
import com.ringoid.origin.model.*

data class FeedItemVO(
    override val id: String,
    override val distanceText: String? = null,
    override val images: List<IImage>,
    override val messages: List<Message> = emptyList(),
    var lastOnlineStatusX: OnlineStatus = OnlineStatus.UNKNOWN,
    override var lastOnlineStatus: String? = null,
    override var lastOnlineText: String? = null,
    override val age: Int = DomainUtil.UNKNOWN_VALUE,
    override val children: Int = DomainUtil.UNKNOWN_VALUE,
    override val education: Int = DomainUtil.UNKNOWN_VALUE,
    override val gender: Gender = Gender.UNKNOWN,
    override val hairColor: Int = DomainUtil.UNKNOWN_VALUE,
    override val height: Int = DomainUtil.UNKNOWN_VALUE,
    override val income: Int = DomainUtil.UNKNOWN_VALUE,
    override val property: Int = DomainUtil.UNKNOWN_VALUE,
    override val transport: Int = DomainUtil.UNKNOWN_VALUE,
    override val about: String? = null,
    override val company: String? = null,
    override val jobTitle: String? = null,
    override val name: String? = null,
    override val instagram: String? = null,
    override val tiktok: String? = null,
    override val university: String? = null,
    override val whereFrom: String? = null,
    override val whereLive: String? = null,
    val isNotSeen: Boolean = false,
    override val isRealModel: Boolean = true,
    var positionOfImage: Int = 0) : IFeedItem {

    constructor(feedItem: FeedItem): this(
        id = feedItem.id,
        distanceText = feedItem.distanceText,
        images = feedItem.images,
        messages = feedItem.messages,
        lastOnlineStatusX = OnlineStatus.from(feedItem.lastOnlineStatus, label = feedItem.lastOnlineText),
        lastOnlineStatus = feedItem.lastOnlineStatus,
        lastOnlineText = feedItem.lastOnlineText,
        isNotSeen = feedItem.isNotSeen,
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
        university = feedItem.university,
        whereFrom = feedItem.whereFrom,
        whereLive = feedItem.whereLive)

    constructor(profile: Profile): this(
        id = profile.id,
        distanceText = profile.distanceText,
        images = profile.images,
        lastOnlineStatusX = OnlineStatus.from(profile.lastOnlineStatus),
        lastOnlineStatus = profile.lastOnlineStatus,
        lastOnlineText = profile.lastOnlineText,
        age = profile.age,
        children = profile.children,
        education = profile.education,
        gender = profile.gender,
        hairColor = profile.hairColor,
        height = profile.height,
        income = profile.income,
        property = profile.property,
        transport = profile.transport,
        about = profile.about,
        company = profile.company,
        jobTitle = profile.jobTitle,
        name = profile.name,
        instagram = profile.instagram,
        tiktok = profile.tiktok,
        university = profile.university,
        whereFrom = profile.whereFrom,
        whereLive = profile.whereLive)

    fun setOnlineStatus(onlineStatus: OnlineStatus?) {
        onlineStatus?.let {
            lastOnlineStatusX = it
            lastOnlineStatus = it.str
            lastOnlineText = it.label
        }
    }

    // property accessors
    fun children(): ChildrenProfileProperty = ChildrenProfileProperty.from(children)
    fun education(): EducationProfileProperty = EducationProfileProperty.from(education)
    fun hairColor(): HairColorProfileProperty = HairColorProfileProperty.from(hairColor)
    fun income(): IncomeProfileProperty = IncomeProfileProperty.from(income)
    fun property(): PropertyProfileProperty = PropertyProfileProperty.from(property)
    fun transport(): TransportProfileProperty = TransportProfileProperty.from(transport)

    fun hashIdWithFirst4(): String = "${idWithFirstN()}_${getModelId()}"
}

val EmptyFeedItemVO = FeedItemVO(feedItem = EmptyFeedItem)
