package com.ringoid.origin.feed.model

import com.ringoid.domain.DomainUtil
import com.ringoid.domain.misc.Gender
import com.ringoid.domain.model.feed.EmptyFeedItem
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.IFeedItem
import com.ringoid.domain.model.feed.Profile
import com.ringoid.domain.model.image.IImage
import com.ringoid.domain.model.messenger.Message

data class FeedItemVO(
    override val id: String,
    override val distanceText: String? = null,
    override val images: List<IImage>,
    override val messages: MutableList<Message> = mutableListOf(),
    val lastOnlineStatusX: OnlineStatus = OnlineStatus.UNKNOWN,
    override val lastOnlineStatus: String? = null,
    override val lastOnlineText: String? = null,
    override val age: Int = DomainUtil.UNKNOWN_VALUE,
    override val education: Int = DomainUtil.UNKNOWN_VALUE,
    override val gender: Gender = Gender.UNKNOWN,
    override val hairColor: Int = DomainUtil.UNKNOWN_VALUE,
    override val height: Int = DomainUtil.UNKNOWN_VALUE,
    override val income: Int = DomainUtil.UNKNOWN_VALUE,
    override val property: Int = DomainUtil.UNKNOWN_VALUE,
    override val transport: Int = DomainUtil.UNKNOWN_VALUE,
    val isNotSeen: Boolean = false,
    override val isRealModel: Boolean = true,
    val likedImages: MutableMap<String, Boolean> = mutableMapOf(),
    var positionOfImage: Int = 0) : IFeedItem {

    constructor(feedItem: FeedItem): this(
        id = feedItem.id,
        distanceText = feedItem.distanceText,
        images = feedItem.images,
        messages = feedItem.messages,
        lastOnlineStatusX = OnlineStatus.from(feedItem.lastOnlineStatus),
        lastOnlineStatus = feedItem.lastOnlineStatus,
        lastOnlineText = feedItem.lastOnlineText,
        isNotSeen = feedItem.isNotSeen,
        age = feedItem.age,
        education = feedItem.education,
        gender = feedItem.gender,
        hairColor = feedItem.hairColor,
        height = feedItem.height,
        income = feedItem.income,
        property = feedItem.property,
        transport = feedItem.transport)

    constructor(profile: Profile): this(
        id = profile.id,
        distanceText = profile.distanceText,
        images = profile.images,
        lastOnlineStatusX = OnlineStatus.from(profile.lastOnlineStatus),
        lastOnlineStatus = profile.lastOnlineStatus,
        lastOnlineText = profile.lastOnlineText,
        age = profile.age,
        education = profile.education,
        gender = profile.gender,
        hairColor = profile.hairColor,
        height = profile.height,
        income = profile.income,
        property = profile.property,
        transport = profile.transport)

    fun isLiked(imageId: String): Boolean = likedImages[imageId] ?: false
    fun hasLiked(): Boolean = likedImages.count { it.value } > 0

    fun hashIdWithFirst4(): String = "${idWithFirstN()}_${getModelId()}"
}

val EmptyFeedItemVO = FeedItemVO(feedItem = EmptyFeedItem)
