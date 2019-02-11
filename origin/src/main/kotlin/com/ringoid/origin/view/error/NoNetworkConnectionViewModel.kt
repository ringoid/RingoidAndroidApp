package com.ringoid.origin.view.error

import android.app.Application
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import javax.inject.Inject

class NoNetworkConnectionViewModel @Inject constructor(app: Application) : BaseViewModel(app) {

    fun onPageReload() {
        if (connectionManager.isNetworkAvailable()) viewState.value = ViewState.CLOSE
        else viewState.value = ViewState.IDLE
    }
}
