package com.ringoid.origin.view.main

import android.app.Application
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.origin.viewmodel.BasePermissionViewModel

abstract class BaseMainViewModel(app: Application) : BasePermissionViewModel(app) {

    private var stopAppTs: Long = 0L

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    open fun onAppReOpen() {
        val elapsed = (System.currentTimeMillis() - stopAppTs) / 60_1000L
        Bus.post(event = BusEvent.ReOpenApp(minutesElapsed = elapsed))
    }

    override fun onStop() {
        super.onStop()
        stopAppTs = System.currentTimeMillis()
    }
}
