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
    override val isRealModel: Boolean = true) : IFeedItem {

    constructor(feedItem: FeedItem): this(id = feedItem.id, images = feedItem.images, messages = feedItem.messages)

    constructor(profile: Profile): this(id = profile.id, images = profile.images)
}

val EmptyFeedItemVO = FeedItemVO(feedItem = EmptyFeedItem)
