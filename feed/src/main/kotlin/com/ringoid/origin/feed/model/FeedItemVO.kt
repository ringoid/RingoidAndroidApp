package com.ringoid.origin.feed.model

import com.ringoid.domain.model.feed.EmptyFeedItem
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.IFeedItem
import com.ringoid.domain.model.feed.Profile
import com.ringoid.domain.model.image.IImage
import com.ringoid.domain.model.messenger.Message

data class FeedItemVO(
    override val id: String, override val images: List<IImage>,
    override val messages: MutableList<Message> = mutableListOf(),
    override val isRealModel: Boolean = true,
    val likedImages: MutableMap<String, Boolean> = mutableMapOf(),
    var positionOfImage: Int = 0) : IFeedItem {

    constructor(feedItem: FeedItem): this(id = feedItem.id, images = feedItem.images, messages = feedItem.messages)

    constructor(profile: Profile): this(id = profile.id, images = profile.images)

    fun isLiked(imageId: String): Boolean = likedImages[imageId] ?: false
    fun hasLiked(): Boolean = likedImages.count { it.value } > 0

    fun hashIdWithFirst7(): String = "${id.substring(0..7)}_${getModelId()}"
}

val EmptyFeedItemVO = FeedItemVO(feedItem = EmptyFeedItem)
