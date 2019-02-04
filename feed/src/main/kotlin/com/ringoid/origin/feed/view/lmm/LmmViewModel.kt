package com.ringoid.origin.feed.view.lmm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.origin.ScreenHelper
import com.uber.autodispose.lifecycle.autoDisposable
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject

class LmmViewModel @Inject constructor(
    val getLmmUseCase: GetLmmUseCase, app: Application) : BaseViewModel(app) {

    val badgeLikes by lazy { MutableLiveData<Boolean>() }
    val badgeMatches by lazy { MutableLiveData<Boolean>() }
    val badgeMessenger by lazy { MutableLiveData<Boolean>() }
    val listScrolls by lazy { MutableLiveData<Int>() }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventRefreshOnProfile(event: BusEvent.RefreshOnProfile) {
        Timber.d("Received bus event: $event")
        // refresh on Profile screen leads Lmm screen to refresh
        getLmm()
    }

    private fun getLmm() {
        val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
        getLmmUseCase.source(params = params)
            .doOnSuccess {
                badgeLikes.value = it.newLikesCount() > 0
                badgeMatches.value = it.newMatchesCount() > 0
                badgeMessenger.value = it.hasNewMessages
            }
            .autoDisposable(this)
            .subscribe({ Timber.v("Lmm has been refreshed") }, Timber::e)
    }

    // --------------------------------------------------------------------------------------------
    fun onTabReselect() {
        listScrolls.value = 0  // scroll to top position
    }
}
