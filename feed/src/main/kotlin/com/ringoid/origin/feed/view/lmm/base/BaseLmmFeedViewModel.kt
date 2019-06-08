package com.ringoid.origin.feed.view.lmm.base

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.manager.analytics.Analytics
import com.ringoid.base.view.ViewState
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.exception.ThresholdExceededException
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.feed.property.*
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesForChatUseCase
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.view.FeedViewModel
import com.ringoid.origin.feed.view.REFRESH
import com.ringoid.origin.feed.view.lmm.RESTORE_CACHED_LIKES
import com.ringoid.origin.feed.view.lmm.RESTORE_CACHED_USER_MESSAGES
import com.ringoid.origin.feed.view.lmm.SEEN_ALL_FEED
import com.ringoid.origin.utils.ScreenHelper
import com.ringoid.origin.view.main.LmmNavTab
import com.ringoid.utility.runOnUiThread
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

/**
 * Cached feed represented with [Lmm] is used when fetching of original feed has failed,
 * basically due to exceeded request threshold, that is actually expressed via [ThresholdExceededException].
 * But this is only done upon refreshing the feed. Before that user could like some feed items
 * and / or compose the very first message to some peers (which then changes appearance of chat icon).
 * Thus, upon failed to refresh, feed is restored from the cache, but that cache lacks this information,
 * so that information is kept in special sources such as [getLikedFeedItemIdsUseCase] and [messagedFeedItemIds]
 * and hence could be applied on a cached feed, restoring the information to user.
 */
abstract class BaseLmmFeedViewModel(
    protected val getLmmUseCase: GetLmmUseCase,
    private val getCachedFeedItemByIdUseCase: GetCachedFeedItemByIdUseCase,
    private val getLikedFeedItemIdsUseCase: GetLikedFeedItemIdsUseCase,
    private val getUserMessagedFeedItemIdsUseCase: GetUserMessagedFeedItemIdsUseCase,
    private val addLikedImageForFeedItemIdUseCase: AddLikedImageForFeedItemIdUseCase,
    private val addUserMessagedFeedItemIdUseCase: AddUserMessagedFeedItemIdUseCase,
    private val notifyLmmProfileBlockedUseCase: NotifyProfileBlockedUseCase,
    private val updateFeedItemAsSeenUseCase: UpdateFeedItemAsSeenUseCase,
    private val transferFeedItemUseCase: TransferFeedItemUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    clearMessagesForChatUseCase: ClearMessagesForChatUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
    userInMemoryCache: IUserInMemoryCache, app: Application)
    : FeedViewModel(
        clearCachedAlreadySeenProfileIdsUseCase,
        clearMessagesForChatUseCase,
        cacheBlockedProfileIdUseCase,
        countUserImagesUseCase,
        userInMemoryCache, app) {

    val feed by lazy { MutableLiveData<List<FeedItemVO>>() }

    protected var badgeIsOn: Boolean = false  // indicates that there are new feed items
        private set
    private val discardedFeedItemIds = mutableSetOf<String>()
    private var notSeenFeedItemIds = mutableSetOf<String>()

    private val badgeIsOnDisposable: Disposable
    private val sourceFeedDisposable: Disposable

    init {
        badgeIsOnDisposable = sourceBadge()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeIsOn = it }, Timber::e)

        sourceFeedDisposable = sourceFeed()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { setLmmItems(items = it, clearMode = ViewState.CLEAR.MODE_EMPTY_DATA) }
            .doAfterNext {
                // analyze for the first reply in messages only once per user session
                if (!analyticsManager.hasFiredOnce(Analytics.AHA_FIRST_REPLY_RECEIVED)) {
                    for (i in 0 until it.size) {
                        if (it[i].countOfUserMessages() > 0) {
                            val userMessageIndex = it[i].messages.indexOfFirst { it.isUserMessage() }
                            val peerMessageIndex = it[i].messages.indexOfLast { it.isPeerMessage() }
                            if (peerMessageIndex > userMessageIndex) {
                                analyticsManager.fireOnce(Analytics.AHA_FIRST_REPLY_RECEIVED, "sourceFeed" to getFeedName())
                            }
                        }
                    }
                }
            }
            .flatMap {
                // get cached feed items that were liked, to restore likes on cached feed
                val params = Params().put("feedItemIds", it.map { it.id })
                getLikedFeedItemIdsUseCase.source(params = params).toObservable()
            }
            .doOnNext { it.ids.takeIf { it.isNotEmpty() }?.let { viewState.value = ViewState.DONE(RESTORE_CACHED_LIKES(it)) } }
            .flatMap {
                // cached feed items that have only user messages inside, sent after receiving feed
                getUserMessagedFeedItemIdsUseCase.source().toObservable()
            }
            .doOnNext { it.takeIf { it.isNotEmpty() }?.let { viewState.value = ViewState.DONE(RESTORE_CACHED_USER_MESSAGES(it)) } }
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    // ------------------------------------------
    protected open fun countNotSeen(feed: List<FeedItem>): List<String> =
        feed.filter { it.isNotSeen }.map { it.id }

    protected abstract fun getFeedFlag(): Int
    protected abstract fun getFeedFromLmm(lmm: Lmm): List<FeedItem>
    protected abstract fun sourceBadge(): Observable<Boolean>
    protected abstract fun sourceFeed(): Observable<List<FeedItem>>

    override fun getFeed() {
        val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
                             .put("source", getFeedName())

        getLmmUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    fun applyCachedFeed(lmm: Lmm?) {
        lmm?.let { setLmmItems(getFeedFromLmm(it)) } ?: run { setLmmItems(emptyList()) }
    }

    fun prependProfileOnTransfer(profileId: String, destinationFeed: LmmNavTab, payload: Bundle? = null, action: (() -> Unit)? = null) {
        // update 'sourceFeed' for feed item (given by 'profileId') in cache to reflect changes locally
        transferFeedItemUseCase.source(Params().put("profileId", profileId).put("destinationFeed", destinationFeed.feedName))
            .andThen(getCachedFeedItemByIdUseCase.source(Params().put("profileId", profileId)))
            .doOnSuccess { feedItem ->
                val list = mutableListOf<FeedItemVO>().apply {
                    val item = FeedItemVO(feedItem).apply {
                        payload?.getInt("positionOfImage", DomainUtil.BAD_POSITION)
                            ?.takeIf { it != DomainUtil.BAD_POSITION }
                            ?.let { this.positionOfImage = it }
                    }
                    add(item)  // prepend transitioned item
                    /**
                     * Add the rest items, but remove previously discarded items, if any.
                     */
                    feed.value  // current list prior to prepending transitioned item
                        ?.toMutableList()
                        ?.let { list -> list.removeAll { it.id in discardedFeedItemIds }; list }
                        ?.let { list -> addAll(list) }
                        ?.also { discardedFeedItemIds.clear() }
                }
                feed.value = list  // prepended list
            }
            .autoDisposable(this)
            .subscribe({ action?.invoke() }, Timber::e)
    }

    private fun setLmmItems(items: List<FeedItem>, clearMode: Int = ViewState.CLEAR.MODE_NEED_REFRESH) {
        discardedFeedItemIds.clear()
        notSeenFeedItemIds.clear()  // clear list of not seen profiles every time Feed is refreshed

        if (items.isEmpty()) {
            viewState.value = ViewState.CLEAR(mode = clearMode)
        } else {
            if (BuildConfig.DEBUG) {
                Timber.v(items.joinToString("\n\t\t", "\t*** LMM ***\n\t\t", "\n\t***\n", transform = { "LMM: ${it.toShortString()} :: ${getFeedName()}" }))
            }
            feed.value = items.map { FeedItemVO(it) }
            viewState.value = ViewState.IDLE
            notSeenFeedItemIds.addAll(countNotSeen(items))
            DebugLogUtil.b("Not seen profiles [${getFeedName()}]: ${notSeenFeedItemIds.joinToString(",", "[", "]", transform = { it.substring(0..3) })}")
        }
    }

    // ------------------------------------------
    override fun onRefresh() {
        super.onRefresh()
        refreshOnPush.value = false  // hide 'tap-to-refresh' upon manual refresh
    }

    internal fun onTapToRefreshClick() {
        Bus.post(BusEvent.RefreshOnPush)
        analyticsManager.fire(Analytics.TAP_TO_REFRESH, "sourceFeed" to getFeedName())
        viewState.value = ViewState.DONE(REFRESH)
    }

    // ------------------------------------------
    protected fun markFeedItemAsSeen(feedItemId: String) {
        if (notSeenFeedItemIds.isEmpty()) {
            return
        }

        updateFeedItemAsSeenUseCase.source(params = Params().put("feedItemId", feedItemId).put("isNotSeen", false))
            .autoDisposable(this)
            .subscribe({}, Timber::e)

        if (notSeenFeedItemIds.remove(feedItemId)) {
            DebugLogUtil.b("Seen [${feedItemId.substring(0..3)}]. Left not seen [${getFeedName()}]: ${notSeenFeedItemIds.joinToString(",", "[", "]", transform = { it.substring(0..3) })}")
            if (notSeenFeedItemIds.isEmpty()) {
                DebugLogUtil.b("All seen [${getFeedName()}]")
                runOnUiThread {
                    viewState.value = ViewState.DONE(SEEN_ALL_FEED(getFeedFlag()))
                    viewState.value = ViewState.IDLE
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventRefreshOnPush(event: BusEvent.RefreshOnPush) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        refreshOnPush.value = false  // drop value on each Lmm feed, when user taps on 'tap to refresh' popup on any Lmm feed
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCleared() {
        super.onCleared()
        badgeIsOnDisposable.dispose()
        sourceFeedDisposable.dispose()
    }

    /* Action Objects */
    // --------------------------------------------------------------------------------------------
    override fun onLike(profileId: String, imageId: String, isLiked: Boolean) {
        super.onLike(profileId, imageId, isLiked)
        addLikedImageForFeedItemIdUseCase.source(params = Params().put("feedItemId", profileId).put("imageId", imageId))
            .autoDisposable(this)
            .subscribe({}, Timber::e)

        markFeedItemAsSeen(feedItemId = profileId)
    }

    override fun onBlock(profileId: String, imageId: String, sourceFeed: String, fromChat: Boolean) {
        super.onBlock(profileId, imageId, sourceFeed, fromChat)
        markFeedItemAsSeen(feedItemId = profileId)
    }

    override fun onReport(profileId: String, imageId: String, reasonNumber: Int, sourceFeed: String, fromChat: Boolean) {
        super.onReport(profileId, imageId, reasonNumber, sourceFeed, fromChat)
        markFeedItemAsSeen(feedItemId = profileId)

        notifyLmmProfileBlockedUseCase.source()  // notify listeners that profile has been blocked
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    override fun onDiscardProfile(profileId: String) {
        super.onDiscardProfile(profileId)
        discardedFeedItemIds.add(profileId)
    }

    open fun onFirstUserMessageSent(profileId: String) {
        addUserMessagedFeedItemIdUseCase.source(params = Params().put("feedItemId", profileId))
            .autoDisposable(this)
            .subscribe({}, Timber::e)  // keep those peers that user has sent the first message to
    }
}
