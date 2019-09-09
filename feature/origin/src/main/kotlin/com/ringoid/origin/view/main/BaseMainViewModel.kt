package com.ringoid.origin.view.main

import android.app.Application
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.origin.AppInMemory
import com.ringoid.origin.viewmodel.BasePermissionViewModel

abstract class BaseMainViewModel(app: Application) : BasePermissionViewModel(app) {

    private var stopAppTs: Long = System.currentTimeMillis()

    // --------------------------------------------------------------------------------------------
    open fun onPushOpen() {
        Bus.post(event = BusEvent.ReOpenAppOnPush)
        /**
         * Since [BusEvent.ReOpenAppOnPush] has just been sent, consume time elapsed since previous
         * app stop in order to mitigate side effects of sending [BusEvent.ReStartWithTime]
         * in the following [onStart].
         */
        stopAppTs = System.currentTimeMillis()
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onFreshStart() {
        super.onFreshStart()
        Bus.post(event = BusEvent.AppFreshStart)
        AppInMemory.setUserGender(spm.currentUserGender())
    }

    override fun onStart() {
        super.onStart()
        val elapsed = System.currentTimeMillis() - stopAppTs
        Bus.post(event = BusEvent.ReStartWithTime(elapsed))
    }

    override fun onStop() {
        super.onStop()
        stopAppTs = System.currentTimeMillis()
    }
}
