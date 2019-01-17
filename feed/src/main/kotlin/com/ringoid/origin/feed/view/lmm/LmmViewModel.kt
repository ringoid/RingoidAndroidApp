package com.ringoid.origin.feed.view.lmm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.ScreenHelper
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import javax.inject.Inject

class LmmViewModel @Inject constructor(private val getLmmUseCase: GetLmmUseCase, app: Application) : BaseViewModel(app) {

    val feedLikes by lazy { MutableLiveData<List<FeedItem>>() }
    val feedMatches by lazy { MutableLiveData<List<FeedItem>>() }

    fun getFeed() {
        val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))

        getLmmUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess { viewState.value = ViewState.IDLE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({
                feedLikes.value = it.likes
                feedMatches.value = it.matches
            }, Timber::e)
    }
}
