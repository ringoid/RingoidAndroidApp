package com.ringoid.data.repository.feed

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.ringoid.data.di.PerAlreadySeen
import com.ringoid.data.di.PerBlock
import com.ringoid.data.di.PerLmmLikes
import com.ringoid.data.di.PerLmmMatches
import com.ringoid.data.local.database.dao.feed.FeedDao
import com.ringoid.data.local.database.dao.feed.UserFeedDao
import com.ringoid.data.local.database.dao.image.ImageDao
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.model.feed.FeedItemDbo
import com.ringoid.data.local.database.model.feed.ProfileIdDbo
import com.ringoid.data.local.database.model.image.ImageDbo
import com.ringoid.data.local.database.model.messenger.MessageDbo
import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.remote.model.feed.FeedResponse
import com.ringoid.data.remote.model.feed.LmmResponse
import com.ringoid.data.repository.BaseRepository
import com.ringoid.data.repository.handleError
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.feed.*
import com.ringoid.domain.model.mapList
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.subjects.PublishSubject
import io.sentry.event.Event
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class FeedRepository @Inject constructor(
    private val local: FeedDao,
    private val imagesLocal: ImageDao,
    private val messengerLocal: MessageDao,
    @PerAlreadySeen private val alreadySeenProfilesCache: UserFeedDao,
    @PerBlock private val blockedProfilesCache: UserFeedDao,
    @PerLmmLikes private val newLikesProfilesCache: UserFeedDao,
    @PerLmmMatches private val newMatchesProfilesCache: UserFeedDao,
    cloud: RingoidCloud, spm: ISharedPrefsManager, aObjPool: IActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IFeedRepository {

    private var lmmInMemory: Pair<Lmm, Long>? = null  // Lmm + it's lastActionTime

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

    // ------------------------------------------
    override fun getCachedFeedItemById(id: String): Single<FeedItem> =
        local.feedItem(profileId = id).map { it.map() }

    override fun clearCachedLmm(): Completable = Completable.fromCallable { local.deleteFeedItems() }

    /**
     * Clear data that was used to track profiles that have already contributed to new likes and matches.
     */
    override fun clearCachedLmmProfileIds(): Completable =
        Single.fromCallable { newLikesProfilesCache.deleteProfileIds() }
              .flatMapCompletable { Completable.fromCallable { newMatchesProfilesCache.deleteProfileIds() } }

    // ------------------------------------------
    override fun markFeedItemAsSeen(feedItemId: String, isNotSeen: Boolean): Completable =
        Completable.fromCallable { local.markFeedItemAsSeen(feedItemId, isNotSeen) }

    override fun transferFeedItem(feedItemId: String, destinationFeed: String): Completable =
        Completable.fromCallable { local.updateSourceFeed(feedItemId, destinationFeed) }

    // --------------------------------------------------------------------------------------------
    override val badgeLikes = PublishSubject.create<Boolean>()
    override val badgeMatches = PublishSubject.create<Boolean>()
    override val badgeMessenger = PublishSubject.create<Boolean>()
    override val feedLikes = PublishSubject.create<LmmSlice>()
    override val feedMatches = PublishSubject.create<LmmSlice>()
    override val feedMessages = PublishSubject.create<LmmSlice>()
    override val lmmChanged = PublishSubject.create<Boolean>()
    override val lmmLoadFinish = PublishSubject.create<Int>()
    override val newLikesCount = PublishSubject.create<Int>()
    override val newMatchesCount = PublishSubject.create<Int>()
    override val newMessagesCount = PublishSubject.create<Int>()

    /* Discover (former New Faces) */
    // ------------------------------------------
    override fun getDiscover(resolution: ImageResolution, limit: Int?, filter: Filters?): Single<Feed> {
        val trace = FirebasePerformance.getInstance().newTrace("refresh_discover")
        return aObjPool
            .triggerSource()
            .doOnSubscribe { trace.start() }
            .flatMap { getDiscoverOnly(resolution, limit, filter, lastActionTime = it) }
            .doFinally { trace.stop() }
    }

    private fun getDiscoverOnly(resolution: ImageResolution, limit: Int?, filter: Filters?,
                                lastActionTime: Long, extraTraces: Collection<Trace> = emptyList()): Single<Feed> =
        spm.accessSingle {
            cloud.getDiscover(it.accessToken, resolution, limit, filter, lastActionTime)
                .handleError(tag = "getDiscover($resolution,$limit,lat=$lastActionTime)", traceTag = "feeds/discover", extraTraces = extraTraces)
                .doOnSuccess { if (it.profiles.isEmpty()) SentryUtil.w("No profiles received for Discover") }
                .filterOutDuplicateProfilesFeed()
                .filterOutAlreadySeenProfilesFeed()
                .filterOutBlockedProfilesFeed()
                .cacheNewFacesAsAlreadySeen()
                .map { it.map() }
        }

    /* New Faces */
    // ------------------------------------------
    @Deprecated("LMM -> LC")
    override fun getNewFaces(resolution: ImageResolution, limit: Int?): Single<Feed> {
        val trace = FirebasePerformance.getInstance().newTrace("refresh_new_faces")
        return aObjPool
            .triggerSource()
            .doOnSubscribe { trace.start() }
            .flatMap { getNewFacesOnly(resolution, limit, lastActionTime = it) }
            .doFinally { trace.stop() }
    }

    @Deprecated("LMM -> LC")
    private fun getNewFacesOnly(resolution: ImageResolution, limit: Int?, lastActionTime: Long,
                                extraTraces: Collection<Trace> = emptyList()): Single<Feed> =
        spm.accessSingle {
            cloud.getNewFaces(it.accessToken, resolution, limit, lastActionTime)
                 .handleError(tag = "getNewFaces($resolution,$limit,lat=$lastActionTime)", traceTag = "feeds/get_new_faces", extraTraces = extraTraces)
                 .doOnSuccess { if (it.profiles.isEmpty()) SentryUtil.w("No profiles received for NewFaces") }
                 .filterOutDuplicateProfilesFeed()
                 .filterOutAlreadySeenProfilesFeed()
                 .filterOutBlockedProfilesFeed()
                 .filterOutLMMProfilesFeed()
                 .cacheNewFacesAsAlreadySeen()
                 .map { it.map() }
        }

    /* LMM */
    // ------------------------------------------
    @Deprecated("LMM -> LC")
    override fun getLmm(resolution: ImageResolution, source: String?): Single<Lmm> {
        val trace = FirebasePerformance.getInstance().newTrace("refresh_lmm")
        return aObjPool
            .triggerSource()
            .doOnSubscribe { trace.start() }
            .flatMap { getLmmOnly(resolution, source = source, lastActionTime = it, extraTraces = listOf(trace)) }
            .onErrorResumeNext {
                Timber.e(it)
                SentryUtil.capture(it, message = "Fallback to get cached Lmm", level = Event.Level.WARNING)
                getCachedLmm()
            }
            .doFinally { trace.stop() }
    }

    @Deprecated("LMM -> LC")
    private fun getLmmOnly(resolution: ImageResolution, source: String?, lastActionTime: Long,
                           extraTraces: Collection<Trace> = emptyList()): Single<Lmm> =
        spm.accessSingle {
            cloud.getLmm(it.accessToken, resolution, source, lastActionTime)
                .handleError(tag = "getLmm($resolution,lat=$lastActionTime)", traceTag = "feeds/get_lmm", extraTraces = extraTraces)
                .dropLmmResponseStatsOnSubscribe()
                .filterOutDuplicateProfilesLmmResponse()
//                .detectCollisionProfilesLmmResponse()
                .filterOutBlockedProfilesLmmResponse()
                .map { it.map() }
                .checkForNewFeedItems()  // now notify observers on data's arrived from Server, properties are not applicable on Server's data
                .checkForNewLikes()
                .checkForNewMatches()
                .checkForNewMessages()
                .cacheLmm()  // cache new Lmm data fetched from the Server
                .cacheMessagesFromLmm()
                .doOnSuccess { lmm -> lmmLoadFinish.onNext(lmm.totalCount()) }
        }

    override fun getLmmTotalCount(): Single<Int> = local.countFeedItems()
    override fun getLmmTotalCount(source: String): Single<Int> = local.countFeedItems(source)

    override fun getLmmProfileIds(): Single<List<String>> = local.feedItemIds()

    @Deprecated("LMM -> LC")
    private fun getCachedLmm(): Single<Lmm> =
        getCachedLmmOnly()
            .dropLmmStatsOnSubscribe()
            .doOnSuccess { DebugLogUtil.v("# Cached Lmm: [${it.toLogString()}] before filter out blocked profiles") }
            .filterOutBlockedProfilesLmm()
            .doOnSuccess { DebugLogUtil.v("# Cached Lmm: [${it.toLogString()}] after filtering out blocked profiles") }
            .checkForNewFeedItems()
            .checkForNewLikes()
            .checkForNewMatches()
            .checkForNewMessages()

    @Deprecated("LMM -> LC")
    private fun getCachedLmmOnly(): Single<Lmm> =
        Single.zip(
            local.feedItems(sourceFeed = DomainUtil.SOURCE_FEED_LIKES).map { it.mapList() },
            local.feedItems(sourceFeed = DomainUtil.SOURCE_FEED_MATCHES).map { it.mapList() },
            local.feedItems(sourceFeed = DomainUtil.SOURCE_FEED_MESSAGES).map { it.mapList() },
            Function3 { likes, matches, messages -> Lmm(likes, matches, messages) })

    /* LC (replacing LMM) */
    // ------------------------------------------
    override fun getLc(resolution: ImageResolution, limit: Int?, filter: Filters?, source: String?): Single<Lmm> {
        val trace = FirebasePerformance.getInstance().newTrace("refresh_lc")
        return aObjPool
            .triggerSource()
            .doOnSubscribe { trace.start() }
            .flatMap { getLcOnly(resolution, limit, filter, source, lastActionTime = it, extraTraces = listOf(trace)) }
            .onErrorResumeNext {
                Timber.e(it)
                SentryUtil.capture(it, message = "Fallback to get cached LC", level = Event.Level.WARNING)
                getCachedLc()
            }
            .doFinally { trace.stop() }
    }

    override fun getLcCounters(resolution: ImageResolution, limit: Int?, filter: Filters?, source: String?): Single<Lmm> =
//        aObjPool.triggerSource()
//            .flatMap { lastActionTime ->
                spm.accessSingle {
                    cloud.getLc(it.accessToken, resolution, limit, filter, source, aObjPool.lastActionTime())
                        .filterOutDuplicateProfilesLmmResponse()
                        .filterOutBlockedProfilesLmmResponse()
                        .map { it.map() }
                        .keepLmmInMemory()
                }
//            }

    private fun getLcOnly(resolution: ImageResolution, limit: Int?, filter: Filters?,
                          source: String?, lastActionTime: Long,
                          extraTraces: Collection<Trace> = emptyList()): Single<Lmm> =
        if (lmmInMemory != null && lmmInMemory!!.second >= lastActionTime) {
            DebugLogUtil.i("Use LC that has been filtered recently from the memory cache")
            Single.just(lmmInMemory!!.first)  // use lmm in memory being recently filtered
        } else {
            spm.accessSingle {
                cloud.getLc(it.accessToken, resolution, limit, filter, source, lastActionTime)
                     .handleError(tag = "getLc($resolution,lat=$lastActionTime", traceTag = "feeds/get_lc", extraTraces = extraTraces)
                     .filterOutDuplicateProfilesLmmResponse()
                     .filterOutBlockedProfilesLmmResponse()
                     .map { it.map() }
            }
        }
        .dropLmmStatsOnSubscribe()
        .checkForNewFeedItems()  // now notify observers on data's arrived from Server, properties are not applicable on Server's data
        .checkForNewLikes()
        .checkForNewMessages()
        .cacheLmm()  // cache new LC data fetched from the Server
        .cacheMessagesFromLmm()
        .doOnSuccess { lmm -> lmmLoadFinish.onNext(lmm.totalCount()) }

    private fun getCachedLc(): Single<Lmm> =
        getCachedLcOnly()
            .dropLmmStatsOnSubscribe()
            .filterOutBlockedProfilesLmm()
            .checkForNewFeedItems()
            .checkForNewLikes()
            .checkForNewMessages()

    private fun getCachedLcOnly(): Single<Lmm> =
        Single.zip(
            local.feedItems(sourceFeed = DomainUtil.SOURCE_FEED_LIKES).map { it.mapList() },
            local.feedItems(sourceFeed = DomainUtil.SOURCE_FEED_MESSAGES).map { it.mapList() },
            BiFunction { likes, messages ->
                val totalNotFilteredLikes = (spm as? SharedPrefsManager)?.getTotalNotFilteredLikes() ?: DomainUtil.BAD_VALUE
                val totalNotFilteredMessages = (spm as? SharedPrefsManager)?.getTotalNotFilteredMessages() ?: DomainUtil.BAD_VALUE
                Lmm(likes = likes, matches = emptyList(), messages = messages,
                    totalNotFilteredLikes = totalNotFilteredLikes,
                    totalNotFilteredMessages = totalNotFilteredMessages)
            })

    // --------------------------------------------------------------------------------------------
    protected fun Single<FeedResponse>.filterOutAlreadySeenProfilesFeed(): Single<FeedResponse> =
        filterOutProfilesFeed(idsSource = getAlreadySeenProfileIds().toObservable())

    protected fun Single<FeedResponse>.filterOutBlockedProfilesFeed(): Single<FeedResponse> =
        filterOutProfilesFeed(idsSource = getBlockedProfileIds().toObservable())

    private fun Single<FeedResponse>.filterOutLMMProfilesFeed(): Single<FeedResponse> =
        filterOutProfilesFeed(idsSource = getLmmProfileIds().toObservable())

    private fun Single<FeedResponse>.filterOutProfilesFeed(idsSource: Observable<List<String>>): Single<FeedResponse> =
        toObservable()
        .withLatestFrom(idsSource,
            BiFunction { feed: FeedResponse, blockedIds: List<String> ->
                blockedIds
                    .takeIf { it.isNotEmpty() }
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
    private fun Single<LmmResponse>.detectCollisionProfilesLmmResponse(): Single<LmmResponse> =
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

    private fun Single<LmmResponse>.filterOutBlockedProfilesLmmResponse(): Single<LmmResponse> =
        toObservable()
        .withLatestFrom(getBlockedProfileIds().toObservable(),
            BiFunction { lmm: LmmResponse, blockedIds: List<String> ->
                blockedIds
                    .takeIf { it.isNotEmpty() }
                    ?.let {
                        val likes = lmm.likes.toMutableList().apply { removeAll { it.id in blockedIds } }
                        val matches = lmm.matches.toMutableList().apply { removeAll { it.id in blockedIds } }
                        val messages = lmm.messages.toMutableList().apply { removeAll { it.id in blockedIds } }
                        lmm.copyWith(likes = likes, matches = matches, messages = messages)
                    } ?: lmm
            })
        .single(LmmResponse()  /* by default - empty lmm */)

    private fun Single<Lmm>.filterOutBlockedProfilesLmm(): Single<Lmm> =
        toObservable()
        .withLatestFrom(getBlockedProfileIds().toObservable(),
            BiFunction { lmm: Lmm, blockedIds: List<String> ->
                blockedIds
                    .takeIf { it.isNotEmpty() }
                    ?.let {
                        val likes = lmm.likes.toMutableList().apply { removeAll { it.id in blockedIds } }
                        val matches = lmm.matches.toMutableList().apply { removeAll { it.id in blockedIds } }
                        val messages = lmm.messages.toMutableList().apply { removeAll { it.id in blockedIds } }
                        Lmm(likes = likes, matches = matches, messages = messages)
                    } ?: lmm
            })
        .single(Lmm()  /* by default - empty lmm */)

    private fun Single<LmmResponse>.filterOutDuplicateProfilesLmmResponse(): Single<LmmResponse> =
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
        flatMap { lmm ->
            /**
             * Note that since [MessageDao.insertMessages] inserts only new [MessageDbo] instances
             * ignoring any updates in existing items (comparing them by [MessageDbo.id]), any updates,
             * in particular - change of [MessageDbo.sourceFeed] value due to profile transfer,
             * should be performed in addition.
             */
            val messages = mutableListOf<MessageDbo>()
            lmm.likes.forEach { messages.addAll(it.messages.map { message -> MessageDbo.from(message) }) }
            lmm.matches.forEach { messages.addAll(it.messages.map { message -> MessageDbo.from(message) }) }
            lmm.messages.forEach { messages.addAll(it.messages.map { message -> MessageDbo.from(message) }) }
            Completable.fromCallable { messengerLocal.insertMessages(messages) }  // cache new messages
                       .toSingleDefault(lmm)
        }

    private fun Single<Lmm>.cacheLmm(): Single<Lmm> =
        flatMap { lmm ->
            Single.fromCallable { local.deleteFeedItems() }  // clear old cache before inserting new data
                .flatMap { Single.fromCallable { imagesLocal.deleteImages() } }
                .doOnSuccess {
                    (spm as? SharedPrefsManager)?.let {
                        it.setTotalNotFilteredLikes(lmm.totalNotFilteredLikes)
                        it.setTotalNotFilteredMessages(lmm.totalNotFilteredMessages)
                    }
                }
                .flatMap {
                    val feedItems = mutableListOf<FeedItemDbo>()
                        .apply {
                            addAll(lmm.likes.map { FeedItemDbo.from(it, sourceFeed = DomainUtil.SOURCE_FEED_LIKES) })
                            addAll(lmm.matches.map { FeedItemDbo.from(it, sourceFeed = DomainUtil.SOURCE_FEED_MATCHES) })
                            addAll(lmm.messages.map { FeedItemDbo.from(it, sourceFeed = DomainUtil.SOURCE_FEED_MESSAGES) })
                        }
                    Single.fromCallable { local.addFeedItems(feedItems) }
                }
                .flatMap {
                    val images = mutableListOf<ImageDbo>()
                        .apply {
                            lmm.likes.forEach { feedItem -> addAll(feedItem.images.map { ImageDbo.from(profileId = feedItem.id, image = it) }) }
                            lmm.matches.forEach { feedItem -> addAll(feedItem.images.map { ImageDbo.from(profileId = feedItem.id, image = it) }) }
                            lmm.messages.forEach { feedItem -> addAll(feedItem.images.map { ImageDbo.from(profileId = feedItem.id, image = it) }) }
                        }
                    Single.fromCallable { imagesLocal.addImages(images) }
                }
                .flatMap { Single.just(lmm) }
        }

    private fun Single<Lmm>.keepLmmInMemory(): Single<Lmm> = doAfterSuccess { lmmInMemory = it to aObjPool.lastActionTime() }

    // ------------------------------------------
    private fun Single<Lmm>.checkForNewFeedItems(): Single<Lmm> =
        doOnSuccess { lmmChanged.onNext(it.containsNotSeenItems()) }  // have not seen items

    private fun Single<Lmm>.checkForNewLikes(): Single<Lmm> =
        doOnSuccess {
            badgeLikes.onNext(it.notSeenLikesCount() > 0)
            feedLikes.onNext(LmmSlice(items = it.likes, totalNotFilteredCount = it.totalNotFilteredLikes))
        }
        .zipWith(newLikesProfilesCache.countProfileIds(), BiFunction { lmm: Lmm, count: Int -> lmm to count })
        .flatMap { (lmm, count) ->
            val profiles = lmm.notSeenLikesProfileIds().map { ProfileIdDbo(it) }
            Completable.fromCallable { newLikesProfilesCache.addProfileIds(profiles) }.toSingleDefault(lmm to count)
        }
        .zipWith(newLikesProfilesCache.countProfileIds(),
            BiFunction { (lmm, oldCount), newCount ->
                val diff = newCount - oldCount
                if (diff > 0) { newLikesCount.onNext(diff) }
                DebugLogUtil.v("# Lmm: count of new likes: $diff")
                lmm
            })

    private fun Single<Lmm>.checkForNewMatches(): Single<Lmm> =
        doOnSuccess {
            badgeMatches.onNext(it.notSeenMatchesCount() > 0)
            feedMatches.onNext(LmmSlice(items = it.matches, totalNotFilteredCount = DomainUtil.BAD_VALUE))  // count not supported as 'matches' are deprecated
        }
        .zipWith(newMatchesProfilesCache.countProfileIds(), BiFunction { lmm: Lmm, count: Int -> lmm to count })
        .flatMap { (lmm, count) ->
            val profiles = lmm.notSeenMatchesProfileIds().map { ProfileIdDbo(it) }
            Completable.fromCallable { newMatchesProfilesCache.addProfileIds(profiles) }.toSingleDefault(lmm to count)
        }
        .zipWith(newMatchesProfilesCache.countProfileIds(),
            BiFunction { (lmm, oldCount), newCount ->
                val diff = newCount - oldCount
                if (diff > 0) { newMatchesCount.onNext(diff) }
                DebugLogUtil.v("# Lmm: count of new matches: $diff")
                lmm
            })

    private fun Single<Lmm>.checkForNewMessages(): Single<Lmm> =
        doOnSuccess { feedMessages.onNext(LmmSlice(items = it.messages, totalNotFilteredCount = it.totalNotFilteredMessages)) }
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
                if (peerMessagesCount != 0 && count < peerMessagesCount) {
                    val diff = peerMessagesCount - count
                    badgeMessenger.onNext(true)
                    lmmChanged.onNext(true)  // have new messages from any peer
                    newMessagesCount.onNext(diff)
                    DebugLogUtil.v("# Lmm: count of new messages: $diff")
                }
                lmm
            })

    private fun Single<LmmResponse>.dropLmmResponseStatsOnSubscribe(): Single<LmmResponse> =
        doOnSubscribe {
            badgeLikes.onNext(false)
            badgeMatches.onNext(false)
            badgeMessenger.onNext(false)
            lmmChanged.onNext(false)
        }

    private fun Single<Lmm>.dropLmmStatsOnSubscribe(): Single<Lmm> =
        doOnSubscribe {
            badgeLikes.onNext(false)
            badgeMatches.onNext(false)
            badgeMessenger.onNext(false)
            lmmChanged.onNext(false)
        }
}
