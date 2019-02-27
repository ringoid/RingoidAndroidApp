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

    // --------------------------------------------------------------------------------------------
    val badgeLikes: PublishSubject<Boolean>
    val badgeMatches: PublishSubject<Boolean>
    val badgeMessenger: PublishSubject<Boolean>
    val feedLikes: PublishSubject<List<FeedItem>>
    val feedMatches: PublishSubject<List<FeedItem>>
    val feedMessages: PublishSubject<List<FeedItem>>
    val lmmChanged: PublishSubject<Boolean>

    fun getNewFaces(resolution: ImageResolution, limit: Int?): Single<Feed>

    fun getLmm(resolution: ImageResolution): Single<Lmm>

    fun dropLmmChangedStatus(): Completable
}
