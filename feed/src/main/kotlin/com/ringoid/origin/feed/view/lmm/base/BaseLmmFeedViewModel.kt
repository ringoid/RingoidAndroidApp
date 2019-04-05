package com.ringoid.origin.feed.view.lmm.base

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.LiveEvent
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.exception.ThresholdExceededException
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.DropLmmChangedStatusUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.origin.feed.view.FeedViewModel
import com.ringoid.origin.feed.view.lmm.SEEN_ALL_FEED
import com.ringoid.origin.utils.ScreenHelper
import com.ringoid.utility.runOnUiThread
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

abstract class BaseLmmFeedViewModel(
    protected val getLmmUseCase: GetLmmUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
    dropLmmChangedStatusUseCase: DropLmmChangedStatusUseCase,
    userInMemoryCache: IUserInMemoryCache, app: Application)
    : FeedViewModel(clearCachedAlreadySeenProfileIdsUseCase, cacheBlockedProfileIdUseCase, countUserImagesUseCase,
                    dropLmmChangedStatusUseCase, userInMemoryCache, app) {

    val feed by lazy { MutableLiveData<List<FeedItem>>() }
    private var cachedFeed: List<FeedItem>? = null
    private var notSeenFeedItemIds = mutableSetOf<String>()

    init {
        sourceFeed()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { setLmmItems(items = it, clearMode = ViewState.CLEAR.MODE_EMPTY_DATA) }
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    protected open fun countNotSeen(feed: List<FeedItem>): List<String> =
        feed.filter { it.isNotSeen }.map { it.id }

    protected abstract fun getFeedFlag(): Int
    protected abstract fun getFeedFromLmm(lmm: Lmm): List<FeedItem>
    protected abstract fun sourceFeed(): Observable<List<FeedItem>>

    override fun getFeed() {
        val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
                             .put("source", getFeedName())

        getLmmUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnError {
                if (it is ThresholdExceededException) {
                    oneShot.value = LiveEvent(it)
                    feed.value = cachedFeed
                    viewState.value = ViewState.IDLE
                } else {
                    viewState.value = ViewState.ERROR(it)
                }
            }
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    fun applyCachedFeed(lmm: Lmm?) {
        lmm?.let { setLmmItems(getFeedFromLmm(it)) } ?: run { setLmmItems(emptyList()) }
    }

    private fun setLmmItems(items: List<FeedItem>, clearMode: Int = ViewState.CLEAR.MODE_NEED_REFRESH) {
        if (items.isEmpty()) {
            viewState.value = ViewState.CLEAR(mode = clearMode)
        } else {
            feed.value = items
            viewState.value = ViewState.IDLE
            notSeenFeedItemIds.addAll(countNotSeen(items))
            DebugLogUtil.b("Not seen profiles [${getFeedName()}]: ${notSeenFeedItemIds.joinToString(",", "[", "]", transform = { it.substring(0..3) })}")
        }
    }

    // ------------------------------------------
    protected fun markFeedItemAsSeen(feedItemId: String) {
        if (notSeenFeedItemIds.isEmpty()) {
            return
        }

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
    override fun clearScreen(mode: Int) {
        cachedFeed = feed.value
        super.clearScreen(mode)
    }

    override fun onRefresh() {
        super.onRefresh()
        Bus.post(event = BusEvent.RefreshOnLmm)
    }
}
