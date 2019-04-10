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
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.domain.model.mapList
import com.ringoid.domain.manager.ISharedPrefsManager
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
    override val newLikesCount = PublishSubject.create<Int>()
    override val newMatchesCount = PublishSubject.create<Int>()
    override val newMessagesCount = PublishSubject.create<Int>()

    override fun getNewFaces(resolution: ImageResolution, limit: Int?): Single<Feed> =
        aObjPool.triggerSource().flatMap { getNewFacesOnly(resolution, limit, lastActionTime = it) }

    private fun getNewFacesOnly(resolution: ImageResolution, limit: Int?, lastActionTime: Long): Single<Feed> =
        spm.accessSingle {
            cloud.getNewFaces(it.accessToken, resolution, limit, lastActionTime)
                 .handleError(tag = "getNewFaces($resolution,$limit,lat=${aObjPool.lastActionTime()})")
                 .doOnSuccess {
                     DebugLogUtil.v("# NewFaces: [${it.toLogString()}] as received from Server, before filter out duplicates")
                     if (it.profiles.isEmpty()) SentryUtil.w("No profiles received for NewFaces")
                 }
                 .filterOutDuplicateProfilesFeed()
                 .doOnSuccess { DebugLogUtil.v("# NewFaces: [${it.toLogString()}] before filter out cached/blocked profiles") }
                 .filterOutAlreadySeenProfilesFeed()
                 .filterOutBlockedProfilesFeed()
                 .doOnSuccess { DebugLogUtil.v("# NewFaces: [${it.toLogString()}] after filtering, final") }
                 .cacheNewFacesAsAlreadySeen()
                 .map { it.map() }
        }

    override fun getLmm(resolution: ImageResolution, source: String?): Single<Lmm> =
        aObjPool.triggerSource().flatMap { getLmmOnly(resolution, source = source, lastActionTime = it) }

    private fun getLmmOnly(resolution: ImageResolution, source: String?, lastActionTime: Long): Single<Lmm> =
        spm.accessSingle {
            cloud.getLmm(it.accessToken, resolution, source, lastActionTime)
                 .handleError(tag = "getLmm($resolution,lat=${aObjPool.lastActionTime()})")
                 .doOnSubscribe {
                     badgeLikes.onNext(false)
                     badgeMatches.onNext(false)
                     badgeMessenger.onNext(false)
                     lmmChanged.onNext(false)
                 }
                 .filterOutDuplicateProfilesLmm()
                 .detectCollisionProfilesLmm()
                 .doOnSuccess { DebugLogUtil.v("# Lmm: [${it.toLogString()}] before filter out blocked profiles") }
                 .filterOutBlockedProfilesLmm()
                 .doOnSuccess { DebugLogUtil.v("# Lmm: [${it.toLogString()}] after filtering, final") }
                 .map { it.map() }
                 .doOnSuccess {
                     // clear sent user messages because they will be restored with new Lmm
                     sentMessagesLocal.deleteMessages()

                     val _newLikesCount = it.newLikesCount()
                     val _newMatchesCount = it.newMatchesCount()

                     badgeLikes.onNext(_newLikesCount > 0)
                     badgeMatches.onNext(_newMatchesCount > 0)
                     feedLikes.onNext(it.likes)
                     feedMatches.onNext(it.matches)
                     feedMessages.onNext(it.messages)
                     lmmChanged.onNext(it.containsNotSeenItems())  // have not seen items
                     newLikesCount.onNext(_newLikesCount)
                     newMatchesCount.onNext(_newMatchesCount)
                 }
                .zipWith(messengerLocal.countUnreadMessages(),
                    BiFunction { lmm: Lmm, count: Int ->
                        if (count > 0) {
                            badgeMessenger.onNext(true)
                            lmmChanged.onNext(true)  // have unread messages since last update
                        }
                        lmm
                    })
                .zipWith(messengerLocal.countPeerMessages(),  // old total messages count from any peer
                    BiFunction { lmm: Lmm, count: Int ->
                        val peerMessagesCount = lmm.peerMessagesCount()
                        if (peerMessagesCount != 0 && count != peerMessagesCount) {
                            badgeMessenger.onNext(true)
                            lmmChanged.onNext(true)  // have new messages from any peer
                            newMessagesCount.onNext(peerMessagesCount - count)
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

    private fun Single<FeedResponse>.filterOutProfilesFeed(idsSource: Observable<List<String>>): Single<FeedResponse> =
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

    private fun Single<FeedResponse>.filterOutDuplicateProfilesFeed(): Single<FeedResponse> =
        flatMap { response ->
            val filterFeed = response.profiles.distinctBy { it.id }
                .also { if (it.size != response.profiles.size) SentryUtil.w("Duplicate profiles detected for NewFaces") }
            Single.just(response.copyWith(profiles = filterFeed))
        }

    // ------------------------------------------
    private fun Single<LmmResponse>.detectCollisionProfilesLmm(): Single<LmmResponse> =
        doOnSuccess {
            val totalSize = it.likes.size + it.matches.size + it.messages.size
            val totalSizeDistinct = mutableListOf<String>()
                .apply { addAll(it.likes.map { it.id }) }
                .apply { addAll(it.matches.map { it.id }) }
                .apply { addAll(it.messages.map { it.id }) }
                .distinct().size

            if (totalSize != totalSizeDistinct) {
                SentryUtil.e("Collision for profiles in LMM")
            }
        }

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

    private fun Single<LmmResponse>.filterOutDuplicateProfilesLmm(): Single<LmmResponse> =
        flatMap { response ->
            val message = "Duplicate profiles detected for "
            val filterLikes = response.likes.distinctBy { it.id }
                .also { if (it.size != response.likes.size) SentryUtil.w("$message LikesYou") }
            val filterMatches = response.matches.distinctBy { it.id }
                .also { if (it.size != response.matches.size) SentryUtil.w("$message Matches") }
            val filterMessenger = response.messages.distinctBy { it.id }
                .also { if (it.size != response.messages.size) SentryUtil.w("$message Messages") }
            Single.just(response.copyWith(likes = filterLikes, matches = filterMatches, messages = filterMessenger))
        }

    // --------------------------------------------------------------------------------------------
    private fun Single<Lmm>.cacheMessagesFromLmm(): Single<Lmm> =
        doAfterSuccess {
            val messages = mutableListOf<MessageDbo>()
            it.likes.forEach { messages.addAll(it.messages.map { MessageDbo.from(it, DomainUtil.SOURCE_FEED_LIKES) }) }
            it.matches.forEach { messages.addAll(it.messages.map { MessageDbo.from(it, DomainUtil.SOURCE_FEED_MATCHES) }) }
            it.messages.forEach { messages.addAll(it.messages.map { MessageDbo.from(it, DomainUtil.SOURCE_FEED_MESSAGES) }) }
            messengerLocal.insertMessages(messages)  // cache new messages
        }
}
