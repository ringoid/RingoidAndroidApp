package com.ringoid.origin.feed.view.lmm.base

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.view.ViewState
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.ScreenHelper
import com.ringoid.origin.feed.view.FeedViewModel
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

abstract class BaseLmmFeedViewModel(protected val getLmmUseCase: GetLmmUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase, app: Application)
    : FeedViewModel(cacheBlockedProfileIdUseCase, countUserImagesUseCase, app) {

    val feed by lazy { MutableLiveData<List<FeedItem>>() }

    init {
        sourceFeed()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({
                Timber.v("Received update lmm feed: $it")
                feed.value = it
            }, Timber::e)
    }

    protected abstract fun sourceFeed(): Observable<List<FeedItem>>

    override fun getFeed() {
        val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))

        getLmmUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess {
                viewState.value = if (it.isMessagesEmpty()) ViewState.CLEAR(mode = ViewState.CLEAR.MODE_EMPTY_DATA)
                else ViewState.IDLE
            }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({ Timber.v("Lmm has been loaded") }, Timber::e)
    }
}
