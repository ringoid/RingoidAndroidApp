package com.ringoid.origin.view.main

import android.app.Application
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.viewmodel.advanced.BasePermissionViewModel

abstract class BaseMainViewModel(app: Application) : BasePermissionViewModel(app) {

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    open fun onAppReOpen() {
        Bus.post(event = BusEvent.ReOpenApp)
    }
}
