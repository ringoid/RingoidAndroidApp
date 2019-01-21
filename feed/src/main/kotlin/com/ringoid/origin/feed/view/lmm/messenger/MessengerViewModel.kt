package com.ringoid.origin.feed.view.lmm.messenger

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
import com.ringoid.origin.feed.view.common.FeedControllerDelegate
import com.ringoid.origin.feed.view.common.IFeedController
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

class MessengerViewModel @Inject constructor(
    private val getLmmUseCase: GetLmmUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase, app: Application)
    : FeedViewModel(cacheBlockedProfileIdUseCase, app), IFeedController {

    val feed by lazy { MutableLiveData<List<FeedItem>>() }

    init {
        getLmmUseCase.repository.feedMessages
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe { feed.value = it }
    }

    private val delegate = FeedControllerDelegate(this, countUserImagesUseCase)

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

    override fun getFeedName(): String = "messages"

    // ------------------------------------------
    override fun clearScreen(mode: Int) {
        delegate.clearScreen(mode)
    }

    override fun onRefresh() {
        delegate.onRefresh()
    }
}
