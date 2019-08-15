package com.ringoid.base.view

import com.ringoid.base.viewmodel.SimpleViewModel

abstract class SimpleBaseActivity : BaseActivity<SimpleViewModel>() {

    override fun getVmClass() = SimpleViewModel::class.java
}
