package com.ringoid.origin.view.error

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.base.viewmodel.OneShot
import com.ringoid.utility.delay
import javax.inject.Inject

class NoNetworkConnectionViewModel @Inject constructor(app: Application) : BaseViewModel(app) {

    private val connectionRestoreOneShot by lazy { MutableLiveData<OneShot<Boolean>>() }
    internal fun connectionRestoreOneShot(): LiveData<OneShot<Boolean>> = connectionRestoreOneShot

    internal fun onPageReload() {
        viewState.value = ViewState.LOADING
        delay {
            if (connectionManager.isNetworkAvailable()) {
                connectionRestoreOneShot.value = OneShot(true)
            } else {
                viewState.value = ViewState.IDLE
            }
        }
    }
}
