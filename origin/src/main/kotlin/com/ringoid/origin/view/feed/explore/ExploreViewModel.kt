package com.ringoid.origin.view.feed.explore

import android.app.Application
import com.ringoid.base.view.ViewState
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.GetNewFacesUseCase
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.origin.view.feed.FeedViewModel
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import javax.inject.Inject

class ExploreViewModel @Inject constructor(private val getNewFacesUseCase: GetNewFacesUseCase, app: Application)
    : FeedViewModel(app) {

    override fun getFeed() {
        getNewFacesUseCase.source(params = Params().put(ImageResolution._1440x1920).put("limit", 20))
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess { viewState.value = ViewState.IDLE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({ feed.value = it }, Timber::e)
    }
}
