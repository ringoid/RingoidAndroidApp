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
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.origin.utils.ScreenHelper
import com.ringoid.origin.feed.view.FeedViewModel
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

abstract class BaseLmmFeedViewModel(protected val getLmmUseCase: GetLmmUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase, app: Application)
    : FeedViewModel(clearCachedAlreadySeenProfileIdsUseCase, cacheBlockedProfileIdUseCase, countUserImagesUseCase, app) {

    val feed by lazy { MutableLiveData<List<FeedItem>>() }
    private var cachedFeed: List<FeedItem>? = null

    init {
        sourceFeed()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ feed.value = it }, Timber::e)
    }

    protected abstract fun isLmmEmpty(lmm: Lmm): Boolean
    protected abstract fun sourceFeed(): Observable<List<FeedItem>>

    override fun getFeed() {
        val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))

        getLmmUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess {
                viewState.value = if (isLmmEmpty(it)) ViewState.CLEAR(mode = ViewState.CLEAR.MODE_EMPTY_DATA)
                                  else ViewState.IDLE
            }
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
            .subscribe({ Timber.v("Lmm has been loaded") }, Timber::e)
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
