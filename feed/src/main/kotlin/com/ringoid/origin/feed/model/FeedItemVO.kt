package com.ringoid.origin.feed.model

import com.ringoid.domain.model.feed.EmptyFeedItem
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.IProfile
import com.ringoid.domain.model.image.IImage

data class FeedItemVO(val feedItem: FeedItem, val numberOfLikes: Int = 0) : IProfile {

    override val id: String get() = feedItem.id
    override val images: List<IImage> get() = feedItem.images
    override val isRealModel: Boolean get() = feedItem.isRealModel
}

val EmptyFeedItemVO = FeedItemVO(feedItem = EmptyFeedItem)
