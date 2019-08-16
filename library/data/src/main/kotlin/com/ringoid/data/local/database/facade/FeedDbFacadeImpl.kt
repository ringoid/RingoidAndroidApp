package com.ringoid.data.local.database.facade

import com.ringoid.data.local.database.dao.feed.FeedDao
import com.ringoid.data.local.database.model.feed.FeedItemDbo
import com.ringoid.datainterface.feed.IFeedDbFacade
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.mapList
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedDbFacadeImpl @Inject constructor(private val dao: FeedDao) : IFeedDbFacade {

    override fun addFeedItems(feedItems: Collection<FeedItem>, sourceFeed: String) {
        feedItems.map { FeedItemDbo.from(it, sourceFeed) }.also { dao.addFeedItems(it) }
    }

    override fun countFeedItems(): Single<Int> = dao.countFeedItems()

    override fun countFeedItems(sourceFeed: String): Single<Int> =
        dao.countFeedItems(sourceFeed)

    override fun deleteFeedItems(): Int = dao.deleteFeedItems()

    override fun feedItem(profileId: String): Single<FeedItem> =
        dao.feedItem(profileId).map { it.map() }

    override fun feedItems(sourceFeed: String): Single<List<FeedItem>> =
        dao.feedItems(sourceFeed).map { it.mapList() }

    override fun feedItemIds(): Single<List<String>> = dao.feedItemIds()

    override fun markFeedItemAsSeen(feedItemId: String, isNotSeen: Boolean): Int =
        dao.markFeedItemAsSeen(feedItemId, isNotSeen)

    override fun updateSourceFeed(feedItemId: String, sourceFeed: String): Int =
        dao.updateSourceFeed(feedItemId, sourceFeed)
}
