package com.ringoid.origin.view.main

import android.app.Application
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.SentryUtil

abstract class BaseMainViewModel(app: Application) : BaseViewModel(app) {

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onFreshCreate() {
        super.onFreshCreate()
        SentryUtil.setUser(spm)
    }
}
