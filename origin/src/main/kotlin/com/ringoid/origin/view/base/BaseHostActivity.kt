package com.ringoid.origin.view.base

import com.ringoid.base.view.SimpleBaseActivity
import com.ringoid.origin.R

abstract class BaseHostActivity : SimpleBaseActivity() {

    override fun getLayoutId(): Int? = R.layout.activity_container
}
