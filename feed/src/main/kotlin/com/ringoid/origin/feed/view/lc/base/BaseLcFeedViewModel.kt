package com.ringoid.origin.feed.view.lc.base

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.manager.analytics.Analytics
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLcUseCase
import com.ringoid.domain.interactor.feed.property.GetCachedFeedItemByIdUseCase
import com.ringoid.domain.interactor.feed.property.TransferFeedItemUseCase
import com.ringoid.domain.interactor.feed.property.UpdateFeedItemAsSeenUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesForChatUseCase
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.essence.feed.FilterEssence
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.view.FeedViewModel
import com.ringoid.origin.feed.view.REFRESH
import com.ringoid.origin.feed.view.lmm.SEEN_ALL_FEED
import com.ringoid.origin.utils.ScreenHelper
import com.ringoid.origin.view.main.LcNavTab
import com.ringoid.utility.runOnUiThread
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class BaseLcFeedViewModel(
    protected val getLcUseCase: GetLcUseCase,
    private val getCachedFeedItemByIdUseCase: GetCachedFeedItemByIdUseCase,
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

    val count by lazy { MutableLiveData<Int>() }
    val feed by lazy { MutableLiveData<List<FeedItemVO>>() }

    protected var badgeIsOn: Boolean = false  // indicates that there are new feed items
        private set
    private val discardedFeedItemIds = mutableSetOf<String>()
    private val notSeenFeedItemIds = Collections.newSetFromMap<String>(ConcurrentHashMap())

    protected abstract fun getSourceFeed(): LcNavTab

    init {
        sourceBadge()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeIsOn = it }, Timber::e)

        sourceFeed()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { setLcItems(items = it, clearMode = ViewState.CLEAR.MODE_EMPTY_DATA) }
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
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    // --------------------------------------------------------------------------------------------
    protected abstract fun countNotSeen(feed: List<FeedItem>): List<String>

    protected abstract fun getFeedFlag(): Int
    protected abstract fun getFeedFromLmm(lmm: Lmm): List<FeedItem>
    protected abstract fun sourceBadge(): Observable<Boolean>
    protected abstract fun sourceFeed(): Observable<List<FeedItem>>

    override fun getFeed() {
        val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
                             .put("limit", DomainUtil.LIMIT_PER_PAGE)
                             .put(FilterEssence.create())  // TODO: prepare global filters
                             .put("source", getFeedName())

        getLcUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    private fun refresh() {
        viewState.value = ViewState.DONE(REFRESH)
    }

    private fun prependProfileOnTransfer(profileId: String, destinationFeed: LcNavTab, payload: Bundle? = null, action: (() -> Unit)? = null) {
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
                    Timber.v("Transfer profile [${getSourceFeed().feedName}]: $profileId")
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
                Timber.v("Feed [${getSourceFeed().feedName}] after transfer: ${list.joinToString("\n\t\t", "\n\t\t", transform = { it.id })}")
                feed.value = list  // prepended list
            }
            .autoDisposable(this)
            .subscribe({ action?.invoke() }, Timber::e)
    }

    private fun setLcItems(items: List<FeedItem>, clearMode: Int = ViewState.CLEAR.MODE_NEED_REFRESH) {
        discardedFeedItemIds.clear()
        notSeenFeedItemIds.clear()  // clear list of not seen profiles every time Feed is refreshed

        count.value = items.size
        if (items.isEmpty()) {
            feed.value = emptyList()
            viewState.value = ViewState.CLEAR(mode = clearMode)
        } else {
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
        analyticsManager.fire(Analytics.TAP_TO_REFRESH, "sourceFeed" to getFeedName())
        refreshOnPush.value = false  // drop value on each LC feed, when user taps on 'tap to refresh' popup on any Lmm feed
        viewState.value = ViewState.DONE(REFRESH)
    }

    // ------------------------------------------
    protected fun markFeedItemAsNotSeen(feedItemId: String) {
        if (!notSeenFeedItemIds.add(feedItemId)) {
            return  // feed item has been added already
        }

        val params = Params().put("feedItemId", feedItemId)
                             .put("isNotSeen", true)
        updateFeedItemAsSeenUseCase.source(params = params)
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    protected fun markFeedItemAsSeen(feedItemId: String) {
        if (notSeenFeedItemIds.isEmpty()) {
            return  // nothing to mark as seen
        }

        val params = Params().put("feedItemId", feedItemId)
                             .put("isNotSeen", false)
        updateFeedItemAsSeenUseCase.source(params = params)
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

    /* Action Objects */
    // --------------------------------------------------------------------------------------------
    override fun onLike(profileId: String, imageId: String) {
        super.onLike(profileId, imageId)
        markFeedItemAsSeen(feedItemId = profileId)
    }

    override fun onBlock(profileId: String, imageId: String, sourceFeed: String, fromChat: Boolean) {
        super.onBlock(profileId, imageId, sourceFeed, fromChat)
        markFeedItemAsSeen(feedItemId = profileId)
    }

    override fun onReport(profileId: String, imageId: String, reasonNumber: Int, sourceFeed: String, fromChat: Boolean) {
        super.onReport(profileId, imageId, reasonNumber, sourceFeed, fromChat)
        markFeedItemAsSeen(feedItemId = profileId)
    }

    override fun onDiscardProfile(profileId: String) {
        super.onDiscardProfile(profileId)
        Timber.v("Discard profile [${getSourceFeed().feedName}]: $profileId")
        discardedFeedItemIds.add(profileId)
    }

    /* Event Bus */
    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventRefreshOnExplore(event: BusEvent.RefreshOnExplore) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        // refresh on Explore Feed screen leads Lmm screen to refresh as well
        DebugLogUtil.i("Get LC on refresh Explore Feed [${getFeedName()}]")
        refresh()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventRefreshOnProfile(event: BusEvent.RefreshOnProfile) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        // refresh on Profile screen leads Lmm screen to refresh as well
        DebugLogUtil.i("Get LC on refresh Profile [${getFeedName()}]")
        refresh()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventReOpenApp(event: BusEvent.ReOpenApp) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        DebugLogUtil.i("Get LC on Application reopen [${getFeedName()}]")
        refresh()  // app reopen leads LC screen to refresh as well
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventReStartWithTime(event: BusEvent.ReStartWithTime) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        if (event.msElapsed in 300000L..1557989300340L) {
            DebugLogUtil.i("App last open was more than 5 minutes ago, refresh LC [${getFeedName()}]")
            refresh()  // app reopen leads LC screen to refresh as well
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventTransferProfile(event: BusEvent.TransferProfile) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        val destinationFeed = event.payload.getSerializable("destinationFeed") as LcNavTab
        if (destinationFeed == getSourceFeed()) {
            prependProfileOnTransfer(profileId = event.profileId, destinationFeed = destinationFeed, payload = event.payload)
        }
    }
}
