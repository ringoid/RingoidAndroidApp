package com.ringoid.data.repository.feed

import com.ringoid.data.action_storage.ActionObjectPool
import com.ringoid.data.local.database.dao.feed.UserFeedDao
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.model.feed.ProfileIdDbo
import com.ringoid.data.local.database.model.messenger.MessageDbo
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.remote.model.feed.FeedResponse
import com.ringoid.data.remote.model.feed.LmmResponse
import com.ringoid.data.repository.BaseRepository
import com.ringoid.data.repository.handleError
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.domain.model.mapList
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
open class FeedRepository @Inject constructor(
    private val messengerLocal: MessageDao, @Named("user") private val sentMessagesLocal: MessageDao,
    @Named("alreadySeen") private val alreadySeenProfilesCache: UserFeedDao,
    @Named("block") private val blockedProfilesCache: UserFeedDao,
    cloud: RingoidCloud, spm: ISharedPrefsManager, aObjPool: ActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IFeedRepository {

    // --------------------------------------------------------------------------------------------
    protected fun Single<FeedResponse>.cacheNewFacesAsAlreadySeen(): Single<FeedResponse> =
        doOnSuccess { alreadySeenProfilesCache.addProfileIds(it.profiles.map { ProfileIdDbo(it.id) }) }

    override fun cacheAlreadySeenProfileIds(ids: Collection<String>): Completable =
        Completable.fromCallable { alreadySeenProfilesCache.addProfileIds(ids.map { ProfileIdDbo(it) }) }

    override fun getAlreadySeenProfileIds(): Single<List<String>> =
        alreadySeenProfilesCache.profileIds().map { it.mapList() }

    override fun deleteAlreadySeenProfileIds(): Completable =
        Completable.fromCallable { alreadySeenProfilesCache.deleteProfileIds() }

    // -------------------------------------------
    override fun cacheBlockedProfileId(profileId: String): Completable =
        Completable.fromCallable { blockedProfilesCache.addProfileId(ProfileIdDbo(profileId)) }

    override fun getBlockedProfileIds(): Single<List<String>> =
        blockedProfilesCache.profileIds().map { it.mapList() }

    override fun deleteBlockedProfileIds(): Completable =
        Completable.fromCallable { blockedProfilesCache.deleteProfileIds() }

    // --------------------------------------------------------------------------------------------
    override val badgeLikes = PublishSubject.create<Boolean>()
    override val badgeMatches = PublishSubject.create<Boolean>()
    override val badgeMessenger = PublishSubject.create<Boolean>()
    override val feedLikes = PublishSubject.create<List<FeedItem>>()
    override val feedMatches = PublishSubject.create<List<FeedItem>>()
    override val feedMessages = PublishSubject.create<List<FeedItem>>()
    override val lmmChanged = PublishSubject.create<Boolean>()

    override fun getNewFaces(resolution: ImageResolution, limit: Int?): Single<Feed> =
        aObjPool.triggerSource().flatMap { getNewFacesOnly(resolution, limit, lastActionTime = it) }

    private fun getNewFacesOnly(resolution: ImageResolution, limit: Int?, lastActionTime: Long): Single<Feed> =
        spm.accessSingle {
            cloud.getNewFaces(it.accessToken, resolution, limit, lastActionTime)
                 .handleError(tag = "getNewFaces($resolution,$limit,lat=${aObjPool.lastActionTime})")
                 .filterOutAlreadySeenProfilesFeed()
                 .filterOutBlockedProfilesFeed()
                 .cacheNewFacesAsAlreadySeen()
                 .map { it.map() }
        }

    override fun getLmm(resolution: ImageResolution, source: String?): Single<Lmm> =
        aObjPool.triggerSource().flatMap { getLmmOnly(resolution, source = source, lastActionTime = it) }

    private fun getLmmOnly(resolution: ImageResolution, source: String?, lastActionTime: Long): Single<Lmm> =
        spm.accessSingle {
            cloud.getLmm(it.accessToken, resolution, source, lastActionTime)
                 .handleError(tag = "getLmm($resolution,lat=${aObjPool.lastActionTime})")
                 .doOnSubscribe {
                     badgeLikes.onNext(false)
                     badgeMatches.onNext(false)
                     badgeMessenger.onNext(false)
                     lmmChanged.onNext(false)
                 }
                 .filterOutBlockedProfilesLmm()
                 .map { it.map() }
                 .doOnSuccess {
                     // clear sent user messages because they will be restored with new Lmm
                     sentMessagesLocal.deleteMessages()
                     badgeLikes.onNext(it.newLikesCount() > 0)
                     badgeMatches.onNext(it.newMatchesCount() > 0)
                     feedLikes.onNext(it.likes)
                     feedMatches.onNext(it.matches)
                     feedMessages.onNext(it.messages)
                     lmmChanged.onNext(it.containsNotSeenItems())  // have not seen items
                 }
                .zipWith(messengerLocal.countChatMessages(),  // old total messages count
                    BiFunction { lmm: Lmm, count: Int ->
                        val newCount = lmm.messagesCount()
                        if (newCount != 0 && count != newCount) {
                            badgeMessenger.onNext(true)
                            lmmChanged.onNext(true)  // have new messages
                        }
                        lmm
                    })
                .cacheMessagesFromLmm()
        }

    override fun dropLmmChangedStatus(): Completable =
        Completable.fromCallable {
            badgeLikes.onNext(false)
            badgeMatches.onNext(false)
            badgeMessenger.onNext(false)
            lmmChanged.onNext(false)
        }

    // --------------------------------------------------------------------------------------------
    protected fun Single<FeedResponse>.filterOutAlreadySeenProfilesFeed(): Single<FeedResponse> =
        filterOutProfilesFeed(idsSource = getAlreadySeenProfileIds().toObservable())

    protected fun Single<FeedResponse>.filterOutBlockedProfilesFeed(): Single<FeedResponse> =
        filterOutProfilesFeed(idsSource = getBlockedProfileIds().toObservable())

    protected fun Single<FeedResponse>.filterOutProfilesFeed(idsSource: Observable<List<String>>): Single<FeedResponse> =
        toObservable()
        .withLatestFrom(idsSource,
            BiFunction { feed: FeedResponse, blockedIds: List<String> ->
                blockedIds
                    .takeIf { !it.isEmpty() }
                    ?.let {
                        val l = feed.profiles.toMutableList().apply { removeAll { it.id in blockedIds } }
                        feed.copyWith(profiles = l)
                    } ?: feed
            })
        .single(FeedResponse()  /* by default - empty feed */)

    // ------------------------------------------
    private fun Single<LmmResponse>.filterOutBlockedProfilesLmm(): Single<LmmResponse> =
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

    // --------------------------------------------------------------------------------------------
    private fun Single<Lmm>.cacheMessagesFromLmm(): Single<Lmm> =
        doAfterSuccess {
            val messages = mutableListOf<MessageDbo>()
            it.likes.forEach { messages.addAll(it.messages.map { MessageDbo.from(it, DomainUtil.SOURCE_FEED_LIKES) }) }
            it.matches.forEach { messages.addAll(it.messages.map { MessageDbo.from(it, DomainUtil.SOURCE_FEED_MATCHES) }) }
            it.messages.forEach { messages.addAll(it.messages.map { MessageDbo.from(it, DomainUtil.SOURCE_FEED_MESSAGES) }) }
            messengerLocal.addMessages(messages)  // cache new messages
        }
}
