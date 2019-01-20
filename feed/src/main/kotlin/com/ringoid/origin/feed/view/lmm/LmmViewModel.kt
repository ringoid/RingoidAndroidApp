package com.ringoid.origin.feed.view.lmm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.ScreenHelper
import com.ringoid.origin.feed.view.common.FeedControllerDelegate
import com.ringoid.origin.feed.view.common.IFeedController
import com.uber.autodispose.lifecycle.autoDisposable
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject

class LmmViewModel @Inject constructor(
    private val getLmmUseCase: GetLmmUseCase, private val countUserImagesUseCase: CountUserImagesUseCase,
    app: Application) : BaseViewModel(app), IFeedController {

    private val delegate = FeedControllerDelegate(this, countUserImagesUseCase)

    val feedLikes by lazy { MutableLiveData<List<FeedItem>>() }
    val feedMatches by lazy { MutableLiveData<List<FeedItem>>() }
    val emptyStateLikes: MutableLiveData<ViewState> by lazy { MutableLiveData<ViewState>() }
    val emptyStateMatches: MutableLiveData<ViewState> by lazy { MutableLiveData<ViewState>() }

    override fun getFeed() {
        val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))

        getLmmUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess {
                viewState.value = ViewState.IDLE
                if (it.isLikesEmpty()) {
                    emptyStateLikes.value = ViewState.CLEAR(mode = ViewState.CLEAR.MODE_EMPTY_DATA)
                }
                if (it.isMatchesEmpty()) {
                    emptyStateMatches.value = ViewState.CLEAR(mode = ViewState.CLEAR.MODE_EMPTY_DATA)
                }
            }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({
                feedLikes.value = it.likes
                feedMatches.value = it.matches
            }, Timber::e)
    }

    // ------------------------------------------
    override fun clearScreen(mode: Int) {
        delegate.clearScreen(mode)
    }

    override fun onRefresh() {
        delegate.onRefresh()
    }

    // ------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventRefreshOnProfile(event: BusEvent.RefreshOnProfile) {
        Timber.d("Received bus event: $event")
        onRefresh()  // refresh on Profile screen leads Lmm screen to refresh
    }
}
