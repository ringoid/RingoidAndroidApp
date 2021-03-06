package com.ringoid.repository.feed

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.ringoid.data.handleError
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.model.messenger.MessageDbo
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.datainterface.di.*
import com.ringoid.datainterface.local.feed.IFeedDbFacade
import com.ringoid.datainterface.local.image.IImageDbFacade
import com.ringoid.datainterface.local.messenger.IMessageDbFacade
import com.ringoid.datainterface.local.user.IUserFeedDbFacade
import com.ringoid.datainterface.remote.IRingoidCloudFacade
import com.ringoid.datainterface.remote.model.feed.FeedResponse
import com.ringoid.datainterface.remote.model.feed.LmmResponse
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.feed.*
import com.ringoid.domain.model.messenger.Message
import com.ringoid.domain.repository.feed.IFeedRepository
import com.ringoid.report.exception.InvalidAccessTokenApiException
import com.ringoid.report.exception.NetworkUnexpected
import com.ringoid.report.exception.OldAppVersionApiException
import com.ringoid.report.exception.WrongRequestParamsClientApiException
import com.ringoid.report.log.Report
import com.ringoid.report.log.ReportLevel
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
    @PerLmmMessages private val unreadChatsCache: IUserFeedDbFacade,
    cloud: IRingoidCloudFacade, spm: ISharedPrefsManager, aObjPool: IActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IFeedRepository {

    private var lmmInMemory: LcInMemory? = null  // Lmm + it's lastActionTime + corresponding filters

    // --------------------------------------------------------------------------------------------
    protected fun Single<Feed>.cacheNewFacesAsAlreadySeen(): Single<Feed> =
        doOnSuccess { alreadySeenProfilesCache.addProfileModelIds(it.profiles) }

    override fun cacheAlreadySeenProfileIds(ids: Collection<String>): Completable =
        Completable.fromAction { alreadySeenProfilesCache.addProfileIds(ids) }

    override fun getAlreadySeenProfileIds(): Single<List<String>> = alreadySeenProfilesCache.profileIds()

    override fun deleteAlreadySeenProfileIds(): Completable =
        Completable.fromAction { alreadySeenProfilesCache.deleteProfileIds() }

    // -------------------------------------------
    override fun cacheBlockedProfileId(profileId: String): Completable =
        Completable.fromAction { blockedProfilesCache.addProfileId(profileId) }

    override fun getBlockedProfileIds(): Single<List<String>> =
        blockedProfilesCache.profileIds()

    override fun deleteBlockedProfileIds(): Completable =
        Completable.fromAction { blockedProfilesCache.deleteProfileIds() }

    // ------------------------------------------
    override fun getCachedFeedItemById(id: String): Single<FeedItem> =
        local.feedItem(profileId = id)

    override fun clearCachedLmm(): Completable = Completable.fromAction { local.deleteFeedItems() }

    /**
     * Clear data that was used to track profiles that have already contributed to new likes and matches.
     */
    override fun clearCachedLmmProfileIds(): Completable =
        Completable.fromAction {
            newLikesProfilesCache.deleteProfileIds()
            newMatchesProfilesCache.deleteProfileIds()
            unreadChatsCache.deleteProfileIds()
        }

    override fun clearCachedLmmTotalCounts(): Completable =
        Completable.fromAction {
            feedSharedPrefs.dropTotalNotFilteredLikes()
            feedSharedPrefs.dropTotalNotFilteredMessages()
        }

    // ------------------------------------------
    override fun markFeedItemAsSeen(feedItemId: String, isNotSeen: Boolean): Completable =
        Completable.fromAction { local.markFeedItemAsSeen(feedItemId, isNotSeen) }

    override fun transferFeedItem(feedItemId: String, destinationFeed: String): Completable =
        Completable.fromAction { local.updateSourceFeed(feedItemId, destinationFeed) }

    // --------------------------------------------------------------------------------------------
    private val badgeLikes = PublishSubject.create<Boolean>()  // LMM contains new likes
    private val badgeMatches = PublishSubject.create<Boolean>()  // LMM contains new matches
    private val badgeMessenger = PublishSubject.create<Boolean>()  // LMM contains new messages
    private val feedLikes = PublishSubject.create<LmmSlice>()
    private val feedMessages = PublishSubject.create<LmmSlice>()
    private val lmmLoadFinish = PublishSubject.create<Int>()  // LMM load finished, contains LMM's total count
    private val lmmLoadFailed = PublishSubject.create<Throwable>()  // LMM load failed, fallback to cache
    private val newLikesCount = PublishSubject.create<Int>()  // for particle animation
    private val newMatchesCount = PublishSubject.create<Int>()  // for particle animation
    private val newUnreadChatsCount = PublishSubject.create<Int>()  // for particle animation
    override fun badgeLikesSource(): Observable<Boolean> = badgeLikes.hide()
    override fun badgeMatchesSource(): Observable<Boolean> = badgeMatches.hide()
    override fun badgeMessengerSource(): Observable<Boolean> = badgeMessenger.hide()
    override fun feedLikesSource(): Observable<LmmSlice> = feedLikes.hide()
    override fun feedMessagesSource(): Observable<LmmSlice> = feedMessages.hide()
    override fun lmmLoadFinishSource(): Observable<Int> = lmmLoadFinish.hide()
    override fun lmmLoadFailedSource(): Observable<Throwable> = lmmLoadFailed.hide()
    override fun newLikesCountSource(): Observable<Int> = newLikesCount.hide()
    override fun newMatchesCountSource(): Observable<Int> = newMatchesCount.hide()
    override fun newUnreadChatsCountSource(): Observable<Int> = newUnreadChatsCount.hide()

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
                 .doOnSuccess { if (it.profiles.isEmpty()) Report.w("No profiles received for NewFaces") }
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
                Report.capture(it, message = "Fallback to get cached Lmm", level = ReportLevel.WARNING)
                if (it is NetworkUnexpected) {
                    lmmLoadFailed.onNext(it)  // deliver error to anyone who subscribed to handle it
                }
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
                // now notify observers on data's arrived from Server, properties are not applicable on Server's data
                .notifyLmmUpdate()
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
            .filterOutBlockedProfilesLmm()
            // now notify observers on data's arrived from Cache, properties are not applicable on Cache's data
            .notifyLmmUpdate()
            .checkForNewLikes()
            .checkForNewMatches()
            .checkForNewMessages()

    @Deprecated("LMM -> LC")
    private fun getCachedLmmOnly(): Single<Lmm> =
        Single.zip(
            local.feedItems(sourceFeed = DomainUtil.SOURCE_SCREEN_FEED_LIKES),
            local.feedItems(sourceFeed = DomainUtil.SOURCE_SCREEN_FEED_MATCHES),
            local.feedItems(sourceFeed = DomainUtil.SOURCE_SCREEN_FEED_MESSAGES),
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
                when (it) {
                    // unrecoverable errors
                    is OldAppVersionApiException,
                    is InvalidAccessTokenApiException,
                    is WrongRequestParamsClientApiException -> Single.error<Lmm>(it)
                    else -> {
                        if (it is NetworkUnexpected) {
                            lmmLoadFailed.onNext(it)  // deliver error to anyone who subscribed to handle it
                        }
                        Report.capture(it, message = "Fallback to get cached LC", level = ReportLevel.WARNING)
                        getCachedLc()  // recover with cache
                    }
                }
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
        // now notify observers on data's arrived from Server, properties are not applicable on Server's data
        .notifyLmmUpdate()
        .checkForNewLikes()
        .checkForNewMatches()
        .checkForNewMessages()
        .cacheLmm()  // cache new LC data fetched from the Server
        .cacheMessagesFromLmm()
        .doOnSuccess { lmm -> lmmLoadFinish.onNext(lmm.totalCount()) }

    private fun getCachedLc(): Single<Lmm> =
        getCachedLcOnly()
            .dropLmmStatsOnSubscribe()
            .filterOutBlockedProfilesLmm()
            // now notify observers on data's arrived from Cache, properties are not applicable on Cache's data
            .notifyLmmUpdate()
            .checkForNewLikes()
            .checkForNewMatches()
            .checkForNewMessages()

    private fun getCachedLcOnly(): Single<Lmm> =
        Single.zip(
            local.feedItems(sourceFeed = DomainUtil.SOURCE_SCREEN_FEED_LIKES),
            local.feedItems(sourceFeed = DomainUtil.SOURCE_SCREEN_FEED_MESSAGES),
            BiFunction { likes, messages ->
                val totalNotFilteredLikes = feedSharedPrefs.getTotalNotFilteredLikes()
                val totalNotFilteredMessages = feedSharedPrefs.getTotalNotFilteredMessages()
                if (totalNotFilteredLikes == DomainUtil.BAD_VALUE ||
                    totalNotFilteredMessages == DomainUtil.BAD_VALUE) {
                    Report.w("Cached LC has invalid total counts",
                                 listOf("totalLikes" to "$totalNotFilteredLikes",
                                        "totalMessages" to "$totalNotFilteredMessages",
                                        "likesSize" to "${likes.size}",
                                        "messagesSize" to "${messages.size}"))
                }
                Lmm(likes = likes, matches = emptyList(), messages = messages,
                    totalNotFilteredLikes = maxOf(totalNotFilteredLikes, likes.size),
                    totalNotFilteredMessages = maxOf(totalNotFilteredMessages, messages.size))
            })

    // --------------------------------------------------------------------------------------------
    protected fun Single<FeedResponse>.filterOutAlreadySeenProfilesFeed(): Single<FeedResponse> =
        filterOutProfilesFeed(idsSource = getAlreadySeenProfileIds())

    protected fun Single<FeedResponse>.filterOutBlockedProfilesFeed(): Single<FeedResponse> =
        filterOutProfilesFeed(idsSource = getBlockedProfileIds())

    private fun Single<FeedResponse>.filterOutLMMProfilesFeed(): Single<FeedResponse> =
        filterOutProfilesFeed(idsSource = getLmmProfileIds())

    private fun Single<FeedResponse>.filterOutProfilesFeed(idsSource: Single<List<String>>): Single<FeedResponse> =
        zipWith(idsSource,
            BiFunction { feed: FeedResponse, blockedIds: List<String> ->
                blockedIds
                    .takeIf { it.isNotEmpty() }
                    ?.let {
                        val l = feed.profiles.toMutableList().apply { removeAll { it.id in blockedIds } }
                        feed.copyWith(profiles = l)
                    } ?: feed
            })

    private fun Single<FeedResponse>.filterOutDuplicateProfilesFeed(): Single<FeedResponse> =
        flatMap { response ->
            val filterFeed = response.profiles.distinctBy { it.id }
            if (filterFeed.size != response.profiles.size) {
                Report.w("Duplicate profiles detected for NewFaces", listOf("size in response" to "${response.profiles.size}", "filtered size" to "${filterFeed.size}"))
                Single.just(response.copyWith(profiles = filterFeed))
            } else Single.just(response)
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
                Report.e("Collision for profiles in LMM")
            }
        }

    private fun Single<LmmResponse>.filterOutBlockedProfilesLmmResponse(): Single<LmmResponse> =
        zipWith(getBlockedProfileIds(),
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

    private fun Single<Lmm>.filterOutBlockedProfilesLmm(): Single<Lmm> =
        zipWith(getBlockedProfileIds(),
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

    private fun Single<LmmResponse>.filterOutDuplicateProfilesLmmResponse(): Single<LmmResponse> =
        flatMap { response ->
            var changed = false
            val message = "Duplicate profiles detected for "

            val filterLikes = response.likes.distinctBy { it.id }
            if (filterLikes.size != response.likes.size) {
                Report.w("$message LikesYou", listOf("size in response" to "${response.likes.size}", "filtered size" to "${filterLikes.size}"))
                changed = true
            }

            val filterMatches = response.matches.distinctBy { it.id }
            if (filterMatches.size != response.matches.size) {
                Report.w("$message Matches", listOf("size in response" to "${response.matches.size}", "filtered size" to "${filterMatches.size}"))
                changed = true
            }

            val filterMessenger = response.messages.distinctBy { it.id }
            if (filterMessenger.size != response.messages.size) {
                Report.w("$message Messages", listOf("size in response" to "${response.messages.size}", "filtered size" to "${filterMessenger.size}"))
                changed = true
            }

            if (changed) {
                Single.just(response.copyWith(likes = filterLikes, matches = filterMatches, messages = filterMessenger))
            } else Single.just(response)
        }

    // --------------------------------------------------------------------------------------------
    private fun Single<Lmm>.notifyLmmUpdate(): Single<Lmm> =
        doOnSuccess { lmm ->
            feedLikes.onNext(LmmSlice(items = lmm.likes, totalNotFilteredCount = lmm.totalNotFilteredLikes))
            // 'matches' are deprecated,so no items being emitted here
            feedMessages.onNext(LmmSlice(items = lmm.messages, totalNotFilteredCount = lmm.totalNotFilteredMessages))
        }

    // ------------------------------------------
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
            Completable.fromAction { messengerLocal.insertMessages(messages) }  // cache new messages
                       .toSingleDefault(lmm)
        }

    private fun Single<Lmm>.cacheLmm(): Single<Lmm> =
        flatMap { lmm ->
            Single
                .fromCallable { local.deleteFeedItems() }  // clear old cache before inserting new data
                .flatMap { Single.fromCallable { imagesLocal.deleteImages() } }
                .flatMap {
                    Single.fromCallable {
                        local.addFeedItems(lmm.likes, sourceFeed = DomainUtil.SOURCE_SCREEN_FEED_LIKES)
                        local.addFeedItems(lmm.matches, sourceFeed = DomainUtil.SOURCE_SCREEN_FEED_MATCHES)
                        local.addFeedItems(lmm.messages, sourceFeed = DomainUtil.SOURCE_SCREEN_FEED_MESSAGES)
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
    private fun Single<Lmm>.checkForNewLikes(): Single<Lmm> =
        flatMap { lmm ->
            Completable.fromAction {
                val profiles = lmm.notSeenLikesProfileIds()
                badgeLikes.onNext(profiles.isNotEmpty())
                if (profiles.isNotEmpty()) {
                    newLikesProfilesCache.insertProfileIds(profiles)
                        .also { DebugLogUtil.v("# LC: count of new likes: $it") }
                        .takeIf { it > 0 }
                        ?.let { newLikesCount.onNext(it) }
                }
            }
            .toSingleDefault(lmm)
        }

    private fun Single<Lmm>.checkForNewMatches(): Single<Lmm> =
        flatMap { lmm ->
            Completable.fromAction {
                val profiles = lmm.notSeenMatchesProfileIds()
                badgeMatches.onNext(profiles.isNotEmpty())
                if (profiles.isNotEmpty()) {
                    newMatchesProfilesCache.insertProfileIds(profiles)
                        .also { DebugLogUtil.v("# LC: count of new matches: $it") }
                        .takeIf { it > 0 }
                        ?.let { newMatchesCount.onNext(it) }
                }
            }
            .toSingleDefault(lmm)
        }

    /**
     * Checks whether [Lmm] contains chats that are unread by user. Since this method checks
     * local cache, it must be called before [cacheLmm].
     */
    private fun Single<Lmm>.checkForNewMessages(): Single<Lmm> =
        zipWith(messengerLocal.countUnreadByUserMessages(),  // count unread local messages
            /**
             * [cacheLmm] hasn't been called yet, by convention, so here we checks the old cache,
             * whether it contains messages that are unread by user, through all chats.
             */
            BiFunction { lmm: Lmm, count: Int ->
                badgeMessenger.onNext(count > 0)  // have unread messages since last update
                lmm
            })
        .flatMap { lmm ->
            lmm.messages
                .filter { it.hasUnreadByUserMessages() }
                .map { it.id }
                .takeIf { it.isNotEmpty() }
                ?.let {
                    Single.fromCallable { unreadChatsCache.insertProfileIds(it) }
                          .doOnSuccess {
                              // actually inserted items (no duplicates)
                              DebugLogUtil.v("# LC: count of new unread chats: $it")
                              if (it > 0) {
                                  badgeMessenger.onNext(true)
                                  newUnreadChatsCount.onNext(it)
                              }
                          }
                          .map { lmm }
                } ?: Single.just(lmm)
        }

    private fun Single<LmmResponse>.dropLmmResponseStatsOnSubscribe(): Single<LmmResponse> =
        doOnSubscribe {
            badgeLikes.onNext(false)
            badgeMatches.onNext(false)
            badgeMessenger.onNext(false)
        }

    private fun Single<Lmm>.dropLmmStatsOnSubscribe(): Single<Lmm> =
        doOnSubscribe {
            badgeLikes.onNext(false)
            badgeMatches.onNext(false)
            badgeMessenger.onNext(false)
        }
}
