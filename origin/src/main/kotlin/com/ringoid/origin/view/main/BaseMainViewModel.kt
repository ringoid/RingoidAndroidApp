package com.ringoid.origin.view.main

import android.app.Application
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.manager.analytics.Analytics
import com.ringoid.origin.AppInMemory
import com.ringoid.origin.viewmodel.BasePermissionViewModel
import com.ringoid.utility.image.ImageLoader

abstract class BaseMainViewModel(app: Application) : BasePermissionViewModel(app) {

    private var stopAppTs: Long = System.currentTimeMillis()

    // --------------------------------------------------------------------------------------------
    internal fun onPushOpen() {
        analyticsManager.fire(Analytics.PUSH_OPEN)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onFreshStart() {
        super.onFreshStart()
        AppInMemory.setUserGender(spm.currentUserGender())
    }

    open fun onAppReOpen() {
        ImageLoader.clear(context)
        Bus.post(event = BusEvent.ReOpenApp)
        stopAppTs = System.currentTimeMillis()  // avoid posting both ReOpenApp and ReStartWithTime events at the same time
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
