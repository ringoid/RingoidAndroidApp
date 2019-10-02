package com.ringoid.domain.repository.feed

import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.feed.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

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
    fun badgeLikesSource(): Observable<Boolean>
    fun badgeMatchesSource(): Observable<Boolean>
    fun badgeMessengerSource(): Observable<Boolean>
    fun feedLikesSource(): Observable<LmmSlice>
    fun feedMessagesSource(): Observable<LmmSlice>
    fun lmmLoadFinishSource(): Observable<Int>
    fun lmmLoadFailedSource(): Observable<Throwable>
    fun newLikesCountSource(): Observable<Int>
    fun newMatchesCountSource(): Observable<Int>
    fun newUnreadChatsCountSource(): Observable<Int>

    fun getDiscover(resolution: ImageResolution, limit: Int?, filters: Filters?): Single<Feed>

    @Deprecated("LMM -> LC")
    fun getNewFaces(resolution: ImageResolution, limit: Int?): Single<Feed>

    @Deprecated("LMM -> LC")
    fun getLmm(resolution: ImageResolution, source: String?): Single<Lmm>

    fun getLc(resolution: ImageResolution, limit: Int?, filters: Filters?, source: String?): Single<Lmm>
    fun getLcCounters(resolution: ImageResolution, limit: Int?, filters: Filters?, source: String?): Single<Lmm>

    fun getLmmProfileIds(): Single<List<String>>

    fun onUpdateSomeChatExternal()
}
