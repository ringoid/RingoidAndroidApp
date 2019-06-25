package com.ringoid.domain.repository.feed

import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
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
    fun getCachedFeedItemById(id: String): Single<FeedItem>

    fun clearCachedLmm(): Completable
    fun clearCachedLmmProfileIds(): Completable

    fun markFeedItemAsSeen(feedItemId: String, isNotSeen: Boolean): Completable

    fun transferFeedItem(feedItemId: String, destinationFeed: String): Completable

    // --------------------------------------------------------------------------------------------
    val badgeLikes: PublishSubject<Boolean>  // LMM contains new likes
    val badgeMatches: PublishSubject<Boolean>  // LMM contains new matches
    val badgeMessenger: PublishSubject<Boolean>  // LMM contains new messages
    val feedLikes: PublishSubject<List<FeedItem>>
    val feedMatches: PublishSubject<List<FeedItem>>
    val feedMessages: PublishSubject<List<FeedItem>>
    val lmmChanged: PublishSubject<Boolean>  // LMM contains new data
    val lmmLoadFinish: PublishSubject<Int>  // LMM load finished, contains LMM's total count
    val newLikesCount: PublishSubject<Int>  // for particle animation
    val newMatchesCount: PublishSubject<Int>  // for particle animation
    val newMessagesCount: PublishSubject<Int>  // for particle animation

    // internal control properties
    val profileBlocked: PublishSubject<Int>  // notify when profile has been blocked

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
