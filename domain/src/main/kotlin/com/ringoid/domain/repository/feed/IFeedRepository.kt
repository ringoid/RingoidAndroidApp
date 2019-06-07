package com.ringoid.domain.repository.feed

import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.domain.model.feed.property.LikedFeedItemIds
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

interface IFeedRepository {

    fun cacheAlreadySeenProfileIds(ids: Collection<String>): Completable

    fun getAlreadySeenProfileIds(): Single<List<String>>

    fun deleteAlreadySeenProfileIds(): Completable

    // -------------------------------------------
    fun cacheBlockedProfileId(profileId: String): Completable

    fun getBlockedProfileIds(): Single<List<String>>

    fun deleteBlockedProfileIds(): Completable

    // ------------------------------------------
    fun cacheLikedFeedItemId(feedItemId: String, imageId: String): Completable
    fun cacheLikedFeedItemIds(ids: LikedFeedItemIds): Completable
    fun cacheUserMessagedFeedItemId(feedItemId: String): Completable

    fun getCachedFeedItemById(id: String): Single<FeedItem>
    fun getLikedFeedItemIds(ids: List<String>): Single<LikedFeedItemIds>
    fun getUserMessagedFeedItemIds(): Single<List<String>>

    fun clearCachedLikedFeedItemIds(): Completable
    fun clearCachedUserMessagedFeedItemIds(): Completable
    fun clearCachedLmm(): Completable
    fun clearCachedLmmProfileIds(): Completable

    fun markFeedItemAsSeen(feedItemId: String, isNotSeen: Boolean): Completable

    fun transferFeedItem(feedItemId: String, destinationFeed: String): Completable

    // --------------------------------------------------------------------------------------------
    val badgeLikes: PublishSubject<Boolean>
    val badgeMatches: PublishSubject<Boolean>
    val badgeMessenger: PublishSubject<Boolean>
    val feedLikes: PublishSubject<List<FeedItem>>
    val feedMatches: PublishSubject<List<FeedItem>>
    val feedMessages: PublishSubject<List<FeedItem>>
    val lmmChanged: PublishSubject<Boolean>
    val lmmLoadFinish: PublishSubject<Int>
    val newLikesCount: PublishSubject<Int>
    val newMatchesCount: PublishSubject<Int>
    val newMessagesCount: PublishSubject<Int>

    fun getNewFaces(resolution: ImageResolution, limit: Int?): Single<Feed>

    fun getLmm(resolution: ImageResolution, source: String?): Single<Lmm>

    /**
     * The following methods operate with cached Lmm, which could contain already blocked profiles.
     * Blocked profiles could be filtered out from cached Lmm only on next cache update.
     */
    fun getLmmTotalCount(): Single<Int>
    fun getLmmTotalCount(source: String): Single<Int>

    fun getLmmProfileIds(): Single<List<String>>

    fun dropLmmChangedStatus(): Completable
}
