package com.ringoid.datainterface.local.image

import com.ringoid.domain.model.feed.FeedItem

interface IImageDbFacade {

    fun addImages(feedItem: FeedItem)

    fun addImages(feedItems: Collection<FeedItem>)

    fun deleteImages(): Int
}
