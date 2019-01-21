package com.ringoid.data.repository.feed

import com.ringoid.data.action_storage.ActionObjectPool
import com.ringoid.data.local.database.dao.feed.FeedDao
import com.ringoid.data.local.database.dao.feed.UserFeedDao
import com.ringoid.data.local.database.model.feed.ProfileIdDbo
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.remote.model.feed.FeedResponse
import com.ringoid.data.remote.model.feed.LmmResponse
import com.ringoid.data.repository.BaseRepository
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.domain.model.mapList
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class FeedRepository @Inject constructor(
    private val local: FeedDao, @Named("user") private val cache: UserFeedDao,
    cloud: RingoidCloud, spm: ISharedPrefsManager, aObjPool: ActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IFeedRepository {

    override fun cacheBlockedProfileId(profileId: String): Completable =
        Completable.fromCallable { cache.addBlockedProfileId(ProfileIdDbo(profileId)) }

    override fun getBlockedProfileIds(): Single<List<String>> =
        cache.blockedProfileIds().map { it.mapList() }

    override fun deleteBlockedProfileIds(): Completable =
        Completable.fromCallable { cache.deleteBlockedProfileIds() }

    // --------------------------------------------------------------------------------------------
    override val feedLikes = BehaviorSubject.create<List<FeedItem>>()
    override val feedMatches = BehaviorSubject.create<List<FeedItem>>()
    override val feedMessages = BehaviorSubject.create<List<FeedItem>>()
    override val lmmChanged = PublishSubject.create<Boolean>()
    override val newMessages = PublishSubject.create<Boolean>()

    // TODO: always check db first
    override fun getNewFaces(resolution: ImageResolution, limit: Int?): Single<Feed> =
        spm.accessSingle {
            cloud.getNewFaces(it.accessToken, resolution, limit, lastActionTime = aObjPool.lastActionTime)
                 .filterBlockedProfilesFeed()
                 .map { it.map() }
        }

    override fun getLmm(resolution: ImageResolution): Single<Lmm> =
        spm.accessSingle {
            cloud.getLmm(it.accessToken, resolution, lastActionTime = aObjPool.lastActionTime)
                 .filterBlockedProfilesLmm()
                 .map { it.map() }
                 .doOnSuccess {
                     feedLikes.onNext(it.likes)
                     feedMatches.onNext(it.matches)
                     feedMessages.onNext(it.messages)
                     lmmChanged.onNext(it.containsNotSeenItems())
                     // TODO: newMessages
                 }
        }

    // --------------------------------------------------------------------------------------------
    private fun Single<FeedResponse>.filterBlockedProfilesFeed(): Single<FeedResponse> =
        toObservable()
        .withLatestFrom(getBlockedProfileIds().toObservable(),
            BiFunction { feed: FeedResponse, blockedIds: List<String> ->
                blockedIds
                    .takeIf { !it.isEmpty() }
                    ?.let {
                        val l = feed.profiles.toMutableList().apply { removeAll { it.id in blockedIds } }
                        feed.copyWith(profiles = l)
                    } ?: feed
            })
        .single(FeedResponse()  /* by default - empty feed */)

    private fun Single<LmmResponse>.filterBlockedProfilesLmm(): Single<LmmResponse> =
        toObservable()
        .withLatestFrom(getBlockedProfileIds().toObservable(),
            BiFunction { lmm: LmmResponse, blockedIds: List<String> ->
                blockedIds
                    .takeIf { !it.isEmpty() }
                    ?.let {
                        val likes = lmm.likes.toMutableList().apply { removeAll { it.id in blockedIds } }
                        val matches = lmm.matches.toMutableList().apply { removeAll { it.id in blockedIds } }
                        val messages = lmm.messages.toMutableList().apply { removeAll { it.id in blockedIds } }
                        lmm.copyWith(likes = likes, matches = matches, messages = messages)
                    } ?: lmm
            })
        .single(LmmResponse()  /* by default - empty lmm */)
}