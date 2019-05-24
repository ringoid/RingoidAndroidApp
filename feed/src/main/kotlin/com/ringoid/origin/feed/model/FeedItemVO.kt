package com.ringoid.origin.feed.model

import com.ringoid.domain.model.feed.EmptyFeedItem
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.IFeedItem
import com.ringoid.domain.model.feed.Profile
import com.ringoid.domain.model.image.IImage
import com.ringoid.domain.model.messenger.Message

data class FeedItemVO(
    override val id: String, override val distanceText: String? = null,
    override val images: List<IImage>,
    override val messages: MutableList<Message> = mutableListOf(),
    val lastOnlineStatusX: OnlineStatus = OnlineStatus.UNKNOWN,
    override val lastOnlineStatus: String? = null,
    override val lastOnlineText: String? = null,
    val isNotSeen: Boolean = false, override val isRealModel: Boolean = true,
    val likedImages: MutableMap<String, Boolean> = mutableMapOf(),
    var positionOfImage: Int = 0) : IFeedItem {

    constructor(feedItem: FeedItem): this(
        id = feedItem.id, distanceText = feedItem.distanceText,
        images = feedItem.images, messages = feedItem.messages,
        lastOnlineStatusX = OnlineStatus.from(feedItem.lastOnlineStatus),
        lastOnlineStatus = feedItem.lastOnlineStatus,
        lastOnlineText = feedItem.lastOnlineText,
        isNotSeen = feedItem.isNotSeen)

    constructor(profile: Profile): this(
        id = profile.id, distanceText = profile.distanceText, images = profile.images,
        lastOnlineStatusX = OnlineStatus.from(profile.lastOnlineStatus),
        lastOnlineStatus = profile.lastOnlineStatus,
        lastOnlineText = profile.lastOnlineText)

    fun isLiked(imageId: String): Boolean = likedImages[imageId] ?: false
    fun hasLiked(): Boolean = likedImages.count { it.value } > 0

    fun hashIdWithFirst4(): String = "${idWithFirstN()}_${getModelId()}"
}

val EmptyFeedItemVO = FeedItemVO(feedItem = EmptyFeedItem)
