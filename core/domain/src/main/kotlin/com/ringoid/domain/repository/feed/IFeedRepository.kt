package com.ringoid.domain.repository.feed

import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.feed.*
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
    fun clearCachedLmmTotalCounts(): Completable

    fun markFeedItemAsSeen(feedItemId: String, isNotSeen: Boolean): Completable

    fun transferFeedItem(feedItemId: String, destinationFeed: String): Completable

    // --------------------------------------------------------------------------------------------
    val badgeLikes: PublishSubject<Boolean>  // LMM contains new likes
    val badgeMatches: PublishSubject<Boolean>  // LMM contains new matches
    val badgeMessenger: PublishSubject<Boolean>  // LMM contains new messages
    val feedLikes: PublishSubject<LmmSlice>
    @Deprecated("LMM -> LC")
    val feedMatches: PublishSubject<LmmSlice>  // deprecated, 'matches' are part of 'chats' in LC
    val feedMessages: PublishSubject<LmmSlice>
    val lmmChanged: PublishSubject<Boolean>  // LMM contains new data
    val lmmLoadFinish: PublishSubject<Int>  // LMM load finished, contains LMM's total count
    val lmmLoadFailed: PublishSubject<Throwable>  // LMM load failed, fallback to cache
    val newLikesCount: PublishSubject<Int>  // for particle animation
    val newMatchesCount: PublishSubject<Int>  // for particle animation
    val newMessagesCount: PublishSubject<Int>  // for particle animation

    fun getDiscover(resolution: ImageResolution, limit: Int?, filters: Filters?): Single<Feed>

    @Deprecated("LMM -> LC")
    fun getNewFaces(resolution: ImageResolution, limit: Int?): Single<Feed>

    @Deprecated("LMM -> LC")
    fun getLmm(resolution: ImageResolution, source: String?): Single<Lmm>

    fun getLc(resolution: ImageResolution, limit: Int?, filters: Filters?, source: String?): Single<Lmm>
    fun getLcCounters(resolution: ImageResolution, limit: Int?, filters: Filters?, source: String?): Single<Lmm>

    fun getLmmProfileIds(): Single<List<String>>
}
