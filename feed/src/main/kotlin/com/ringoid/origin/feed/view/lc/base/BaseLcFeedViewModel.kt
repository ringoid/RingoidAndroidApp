package com.ringoid.origin.feed.view.lc.base

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.DropLmmChangedStatusUseCase
import com.ringoid.domain.interactor.feed.GetLcUseCase
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
    private val dropLmmChangedStatusUseCase: DropLmmChangedStatusUseCase,
    private val updateFeedItemAsSeenUseCase: UpdateFeedItemAsSeenUseCase,
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
            // TODO: analyze for the first reply in messages only once per user session
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
        // TODO
    }

    private fun setLcItems(items: List<FeedItem>, clearMode: Int = ViewState.CLEAR.MODE_NEED_REFRESH) {
        // TODO: clear discardedFeedItemIds
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

    /* Event Bus */
    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    override fun onEventNoImagesOnProfile(event: BusEvent.NoImagesOnProfile) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        dropLmmChangedStatusUseCase.source()  // drop changed status (red dot badges)
            .autoDisposable(this)
            .subscribe({ Timber.d("Badges on Lmm have been dropped because no images in user's profile") }, Timber::e)
    }

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
}
