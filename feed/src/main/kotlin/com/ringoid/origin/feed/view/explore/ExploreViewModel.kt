package com.ringoid.origin.feed.view.explore

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.view.ViewState
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.GetNewFacesUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.model.feed.Feed
import com.ringoid.origin.ScreenHelper
import com.ringoid.origin.feed.view.FeedViewModel
import com.ringoid.origin.feed.view.common.FeedControllerDelegate
import com.ringoid.origin.feed.view.common.IFeedController
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import javax.inject.Inject

class ExploreViewModel @Inject constructor(
    private val getNewFacesUseCase: GetNewFacesUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
    app: Application) : FeedViewModel(cacheBlockedProfileIdUseCase, app), IFeedController {

    private val delegate = FeedControllerDelegate(this, countUserImagesUseCase)

    val feed by lazy { MutableLiveData<Feed>() }

    override fun getFeed() {
        val params = Params()
            .put(ScreenHelper.getLargestPossibleImageResolution(context))
            .put("limit", 20)

        getNewFacesUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess {
                viewState.value = if (it.isEmpty()) ViewState.CLEAR(mode = ViewState.CLEAR.MODE_EMPTY_DATA)
                                  else ViewState.IDLE
            }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({ feed.value = it }, Timber::e)
    }

    override fun getFeedName(): String = "new_faces"

    // ------------------------------------------
    override fun clearScreen(mode: Int) {
        delegate.clearScreen(mode)
    }

    override fun onRefresh() {
        delegate.onRefresh()
    }
}
