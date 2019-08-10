package com.ringoid.origin.view.main

import android.app.Application
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.manager.analytics.Analytics
import com.ringoid.base.navigation.AppScreen
import com.ringoid.base.navigation.NavigationRegistry
import com.ringoid.origin.AppInMemory
import com.ringoid.origin.viewmodel.BasePermissionViewModel

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
        if (NavigationRegistry.beforeLastScreen() != AppScreen.LOGIN) {
            Bus.post(event = BusEvent.AppFreshStart)
        }
        AppInMemory.setUserGender(spm.currentUserGender())
    }

    open fun onAppReOpen() {
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
