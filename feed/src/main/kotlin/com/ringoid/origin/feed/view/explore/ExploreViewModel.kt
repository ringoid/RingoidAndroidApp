package com.ringoid.origin.feed.view.explore

import android.app.Application
import com.ringoid.base.view.ViewState
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.GetNewFacesUseCase
import com.ringoid.origin.ScreenHelper
import com.ringoid.origin.feed.view.FeedViewModel
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import javax.inject.Inject

class ExploreViewModel @Inject constructor(private val getNewFacesUseCase: GetNewFacesUseCase, app: Application)
    : FeedViewModel(app) {

    override fun getFeed() {
        val params = Params()
            .put(ScreenHelper.getLargestPossibleImageResolution(context))
            .put("limit", 20)

        getNewFacesUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess { viewState.value = ViewState.IDLE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({ feed.value = it }, Timber::e)
    }

    override fun getFeedName(): String = "new_faces"

    fun onRefresh() {
        viewState.value = ViewState.CLEAR
        getFeed()
    }
}
