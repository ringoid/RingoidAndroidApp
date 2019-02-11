package com.ringoid.origin.view.error

import android.app.Application
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.utility.delay
import javax.inject.Inject

class NoNetworkConnectionViewModel @Inject constructor(app: Application) : BaseViewModel(app) {

    fun onPageReload() {
        viewState.value = ViewState.LOADING
        delay(delay = 1000L) {
            if (connectionManager.isNetworkAvailable()) viewState.value = ViewState.CLOSE
            else viewState.value = ViewState.IDLE
        }
    }
}
