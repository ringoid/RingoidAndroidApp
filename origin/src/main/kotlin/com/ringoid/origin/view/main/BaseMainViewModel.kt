package com.ringoid.origin.view.main

import android.app.Application
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.log.SentryUtil

abstract class BaseMainViewModel(app: Application) : BaseViewModel(app) {

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onFreshStart() {
        super.onFreshStart()
        SentryUtil.setUser(spm)
    }
}
