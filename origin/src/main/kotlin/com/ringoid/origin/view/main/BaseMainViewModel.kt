package com.ringoid.origin.view.main

import android.app.Application
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.viewmodel.BaseViewModel

abstract class BaseMainViewModel(app: Application) : BaseViewModel(app) {

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    open fun onAppReOpen() {
        Bus.post(event = BusEvent.ReOpenApp)
    }
}
