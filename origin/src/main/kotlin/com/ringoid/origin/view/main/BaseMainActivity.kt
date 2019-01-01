package com.ringoid.origin.view.main

import com.ringoid.base.view.BaseActivity
import com.ringoid.origin.R

abstract class BaseMainActivity<VM : BaseMainViewModel> : BaseActivity<VM>() {

    override fun getLayoutId(): Int = R.layout.activity_main

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
}
