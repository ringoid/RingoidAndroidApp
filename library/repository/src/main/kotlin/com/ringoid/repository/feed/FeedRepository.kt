package com.ringoid.repository.feed

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.ringoid.data.handleError
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.model.messenger.MessageDbo
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.datainterface.di.PerAlreadySeen
import com.ringoid.datainterface.di.PerBlock
import com.ringoid.datainterface.di.PerLmmLikes
import com.ringoid.datainterface.di.PerLmmMatches
import com.ringoid.datainterface.local.feed.IFeedDbFacade
import com.ringoid.datainterface.local.image.IImageDbFacade
import com.ringoid.datainterface.local.messenger.IMessageDbFacade
import com.ringoid.datainterface.local.user.IUserFeedDbFacade
import com.ringoid.datainterface.remote.IRingoidCloudFacade
import com.ringoid.datainterface.remote.model.feed.FeedResponse
import com.ringoid.datainterface.remote.model.feed.LmmResponse
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.feed.*
import com.ringoid.domain.model.messenger.Message
import com.ringoid.domain.repository.feed.IFeedRepository
import com.ringoid.repository.BaseRepository
import com.ringoid.repository.FeedSharedPrefs
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class FeedRepository @Inject constructor(
    private val local: IFeedDbFacade,
    private val imagesLocal: IImageDbFacade,
    private val messengerLocal: IMessageDbFacade,
    private val feedSharedPrefs: FeedSharedPrefs,
    @PerAlreadySeen private val alreadySeenProfilesCache: IUserFeedDbFacade,
    @PerBlock private val blockedProfilesCache: IUserFeedDbFacade,
    @PerLmmLikes private val newLikesProfilesCache: IUserFeedDbFacade,
    @PerLmmMatches private val newMatchesProfilesCache: IUserFeedDbFacade,
    cloud: IRingoidCloudFacade, spm: ISharedPrefsManager, aObjPool: IActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IFeedRepository {

    private var lmmInMemory: LcInMemory? = null  // Lmm + it's lastActionTime + corresponding filters

    // --------------------------------------------------------------------------------------------
    protected fun Single<Feed>.cacheNewFacesAsAlreadySeen(): Single<Feed> =
        doOnSuccess { alreadySeenProfilesCache.addProfileModelIds(it.profiles) }

    override fun cacheAlreadySeenProfileIds(ids: Collection<String>): Completable =
        Completable.fromCallable { alreadySeenProfilesCache.addProfileIds(ids) }

    override fun getAlreadySeenProfileIds(): Single<List<String>> = alreadySeenProfilesCache.profileIds()

    override fun deleteAlreadySeenProfileIds(): Completable =
        Completable.fromCallable { alreadySeenProfilesCache.deleteProfileIds() }

    // -------------------------------------------
    override fun cacheBlockedProfileId(profileId: String): Completable =
        Completable.fromCallable { blockedProfilesCache.addProfileId(profileId) }

    override fun getBlockedProfileIds(): Single<List<String>> =
        blockedProfilesCache.profileIds()

    override fun deleteBlockedProfileIds(): Completable =
        Completable.fromCallable { blockedProfilesCache.deleteProfileIds() }

    // ------------------------------------------
    override fun getCachedFeedItemById(id: String): Single<FeedItem> =
        local.feedItem(profileId = id)

    override fun clearCachedLmm(): Completable = Completable.fromCallable { local.deleteFeedItems() }

    /**
     * Clear data that was used to track profiles that have already contributed to new likes and matches.
     */
    override fun clearCachedLmmProfileIds(): Completable =
        Single.fromCallable { newLikesProfilesCache.deleteProfileIds() }
              .flatMapCompletable { Completable.fromCallable { newMatchesProfilesCache.deleteProfileIds() } }

    override fun clearCachedLmmTotalCounts(): Completable =
        Completable.fromCallable {
            feedSharedPrefs.dropTotalNotFilteredLikes()
            feedSharedPrefs.dropTotalNotFilteredMessages()
        }

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
    override fun getDiscover(resolution: ImageResolution, limit: Int?, filters: Filters?): Single<Feed> {
        val trace = FirebasePerformance.getInstance().newTrace("refresh_discover")
        return aObjPool
            .triggerSource()
            .doOnSubscribe { trace.start() }
            .flatMap { getDiscoverOnly(resolution, limit, filters, lastActionTime = it) }
            .doFinally { trace.stop() }
    }

    private fun getDiscoverOnly(resolution: ImageResolution, limit: Int?, filters: Filters?,
                                lastActionTime: Long, extraTraces: Collection<Trace> = emptyList()): Single<Feed> =
        spm.accessSingle {
            cloud.getDiscover(it.accessToken, resolution, limit, filters, lastActionTime)
                .handleError(tag = "getDiscover($resolution,$limit,lat=$lastActionTime)", traceTag = "feeds/discover", extraTraces = extraTraces)
                .doOnSuccess { if (it.profiles.isEmpty()) SentryUtil.w("No profiles received for Discover") }
                .filterOutDuplicateProfilesFeed()
                .filterOutAlreadySeenProfilesFeed()
                .filterOutBlockedProfilesFeed()
                .map { it.map() }
                .cacheNewFacesAsAlreadySeen()
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
                 .map { it.map() }
                 .cacheNewFacesAsAlreadySeen()
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
                SentryUtil.capture(it, message = "Fallback to get cached Lmm", level = SentryUtil.Level.WARNING)
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
            local.feedItems(sourceFeed = DomainUtil.SOURCE_FEED_LIKES),
            local.feedItems(sourceFeed = DomainUtil.SOURCE_FEED_MATCHES),
            local.feedItems(sourceFeed = DomainUtil.SOURCE_FEED_MESSAGES),
            Function3 { likes, matches, messages -> Lmm(likes, matches, messages) })

    /* LC (replacing LMM) */
    // ------------------------------------------
    override fun getLc(resolution: ImageResolution, limit: Int?, filters: Filters?, source: String?): Single<Lmm> {
        val trace = FirebasePerformance.getInstance().newTrace("refresh_lc")
        return aObjPool
            .triggerSource()
            .doOnSubscribe { trace.start() }
            .flatMap { getLcOnly(resolution, limit, filters, source, lastActionTime = it, extraTraces = listOf(trace)) }
            .onErrorResumeNext {
                Timber.e(it)
                SentryUtil.capture(it, message = "Fallback to get cached LC", level = SentryUtil.Level.WARNING)
                getCachedLc()
            }
            .doFinally { trace.stop() }
    }

    override fun getLcCounters(resolution: ImageResolution, limit: Int?, filters: Filters?, source: String?): Single<Lmm> =
//        aObjPool.triggerSource()
//            .flatMap { lastActionTime ->
                spm.accessSingle {
                    cloud.getLc(it.accessToken, resolution, limit, filters, source, aObjPool.lastActionTime())
                        .handleError(count = 8, tag = "getLcCounts($resolution,lat=${aObjPool.lastActionTime()}", traceTag = "feeds/get_lc_counts")
                        .filterOutDuplicateProfilesLmmResponse()
                        .filterOutBlockedProfilesLmmResponse()
                        .map { it.map() }
                        .cacheLmmCounts()
                        .keepLmmInMemory(filters)
                }
//            }

    private fun getLcOnly(resolution: ImageResolution, limit: Int?, filters: Filters?,
                          source: String?, lastActionTime: Long,
                          extraTraces: Collection<Trace> = emptyList()): Single<Lmm> =
        if (lmmInMemory != null && lmmInMemory!!.lastActionTime >= lastActionTime &&
            lmmInMemory!!.filters == filters) {  // use in-memory data only if all criteria fulfill
            DebugLogUtil.i("Use LC that has been filtered recently from the memory cache")
            Single.just(lmmInMemory!!.lmm)  // use LC in memory being recently filtered
        } else {
            spm.accessSingle {
                cloud.getLc(it.accessToken, resolution, limit, filters, source, lastActionTime)
                     .handleError(tag = "getLc($resolution,lat=$lastActionTime", traceTag = "feeds/get_lc", extraTraces = extraTraces)
                     .filterOutDuplicateProfilesLmmResponse()
                     .filterOutBlockedProfilesLmmResponse()
                     .map { it.map() }
                     .cacheLmmCounts()
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
            local.feedItems(sourceFeed = DomainUtil.SOURCE_FEED_LIKES),
            local.feedItems(sourceFeed = DomainUtil.SOURCE_FEED_MESSAGES),
            BiFunction { likes, messages ->
                val totalNotFilteredLikes = feedSharedPrefs.getTotalNotFilteredLikes()
                val totalNotFilteredMessages = feedSharedPrefs.getTotalNotFilteredMessages()
                if (totalNotFilteredLikes == DomainUtil.BAD_VALUE ||
                    totalNotFilteredMessages == DomainUtil.BAD_VALUE) {
                    SentryUtil.w("Cached LC has invalid total counts",
                                 listOf("totalLikes" to "$totalNotFilteredLikes",
                                        "totalMessages" to "$totalNotFilteredMessages"))
                }
                Lmm(likes = likes, matches = emptyList(), messages = messages,
                    totalNotFilteredLikes = maxOf(totalNotFilteredLikes, likes.size),
                    totalNotFilteredMessages = maxOf(totalNotFilteredMessages, messages.size))
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
            val messages = mutableListOf<Message>().apply {
                lmm.likes.forEach { addAll(it.messages) }
                lmm.matches.forEach { addAll(it.messages) }
                lmm.messages.forEach { addAll(it.messages) }
            }
            Completable.fromCallable { messengerLocal.insertMessages(messages) }  // cache new messages
                       .toSingleDefault(lmm)
        }

    private fun Single<Lmm>.cacheLmm(): Single<Lmm> =
        flatMap { lmm ->
            Single
                .fromCallable { local.deleteFeedItems() }  // clear old cache before inserting new data
                .flatMap { Single.fromCallable { imagesLocal.deleteImages() } }
                .flatMap {
                    Single.fromCallable {
                        local.addFeedItems(lmm.likes, sourceFeed = DomainUtil.SOURCE_FEED_LIKES)
                        local.addFeedItems(lmm.matches, sourceFeed = DomainUtil.SOURCE_FEED_MATCHES)
                        local.addFeedItems(lmm.messages, sourceFeed = DomainUtil.SOURCE_FEED_MESSAGES)
                    }
                }
                .flatMap {
                    Single.fromCallable {
                        imagesLocal.addImages(lmm.likes)
                        imagesLocal.addImages(lmm.matches)
                        imagesLocal.addImages(lmm.messages)
                    }
                }
                .flatMap { Single.just(lmm) }
        }

    private fun Single<Lmm>.cacheLmmCounts(): Single<Lmm> =
        doOnSuccess { lmm ->
            feedSharedPrefs.setTotalNotFilteredLikes(lmm.totalNotFilteredLikes)
            feedSharedPrefs.setTotalNotFilteredMessages(lmm.totalNotFilteredMessages)
        }

    private fun Single<Lmm>.keepLmmInMemory(filters: Filters?): Single<Lmm> =
        doAfterSuccess { lmmInMemory = LcInMemory(it, aObjPool.lastActionTime(), filters) }

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
            val profiles = lmm.notSeenLikesProfileIds()
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
            val profiles = lmm.notSeenMatchesProfileIds()
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
                    DebugLogUtil.v("# LC: count of new messages: $diff")
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
