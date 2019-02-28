package com.ringoid.origin.feed.view.lmm.base

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.LiveEvent
import com.ringoid.domain.exception.ThresholdExceededException
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.DropLmmChangedStatusUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.origin.feed.view.FeedViewModel
import com.ringoid.origin.utils.ScreenHelper
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

abstract class BaseLmmFeedViewModel(
    protected val getLmmUseCase: GetLmmUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
    dropLmmChangedStatusUseCase: DropLmmChangedStatusUseCase, app: Application)
    : FeedViewModel(clearCachedAlreadySeenProfileIdsUseCase, cacheBlockedProfileIdUseCase, countUserImagesUseCase, dropLmmChangedStatusUseCase, app) {

    val feed by lazy { MutableLiveData<List<FeedItem>>() }
    private var cachedFeed: List<FeedItem>? = null

    init {
        sourceFeed()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { setLmmItems(items = it, clearMode = ViewState.CLEAR.MODE_EMPTY_DATA) }
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    protected abstract fun getFeedFromLmm(lmm: Lmm): List<FeedItem>
    protected abstract fun sourceFeed(): Observable<List<FeedItem>>

    override fun getFeed() {
        val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))

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
        lmm?.let { setLmmItems(getFeedFromLmm(it)) }
    }

    private fun setLmmItems(items: List<FeedItem>, clearMode: Int = ViewState.CLEAR.MODE_NEED_REFRESH) {
        if (items.isEmpty()) {
            viewState.value = ViewState.CLEAR(mode = clearMode)
        } else {
            feed.value = items
            viewState.value = ViewState.IDLE
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
