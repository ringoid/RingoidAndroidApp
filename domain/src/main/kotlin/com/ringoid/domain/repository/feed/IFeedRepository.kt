package com.ringoid.domain.repository.feed

import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface IFeedRepository {

    fun cacheBlockedProfileId(profileId: String): Completable

    fun getBlockedProfileIds(): Single<List<String>>

    fun deleteBlockedProfileIds(): Completable

    // --------------------------------------------------------------------------------------------
    val feedLikes: BehaviorSubject<List<FeedItem>>
    val feedMatches: BehaviorSubject<List<FeedItem>>
    val feedMessages: BehaviorSubject<List<FeedItem>>
    val hasNotSeenLmm: PublishSubject<Boolean>

    fun getNewFaces(resolution: ImageResolution, limit: Int?): Single<Feed>

    fun getLmm(resolution: ImageResolution): Single<Lmm>
}
