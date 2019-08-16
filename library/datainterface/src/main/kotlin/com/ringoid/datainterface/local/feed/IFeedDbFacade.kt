package com.ringoid.datainterface.local.feed

import com.ringoid.domain.model.feed.FeedItem
import io.reactivex.Single

interface IFeedDbFacade {

    fun addFeedItems(feedItems: Collection<FeedItem>, sourceFeed: String)

    fun countFeedItems(): Single<Int>

    fun countFeedItems(sourceFeed: String): Single<Int>

    fun deleteFeedItems(): Int

    fun feedItem(profileId: String): Single<FeedItem>

    fun feedItems(sourceFeed: String): Single<List<FeedItem>>

    fun feedItemIds(): Single<List<String>>

    fun markFeedItemAsSeen(feedItemId: String, isNotSeen: Boolean): Int

    fun updateSourceFeed(feedItemId: String, sourceFeed: String): Int
}

