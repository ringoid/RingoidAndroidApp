package com.ringoid.main.view

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.origin.view.main.BaseMainViewModel
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject constructor(getLmmUseCase: GetLmmUseCase,
    private val clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    app: Application) : BaseMainViewModel(app) {

    val badgeLmm by lazy { MutableLiveData<Boolean>() }

    init {
        getLmmUseCase.repository.lmmChanged
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeLmm.value = it }, Timber::e)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onFreshCreate() {
        super.onFreshCreate()
        clearCachedAlreadySeenProfileIdsUseCase.source()
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    // --------------------------------------------------------------------------------------------
    fun onRefreshFeed() {
        badgeLmm.value = false  // discard badge on refresh - it will be set properly after refresh
    }
}
