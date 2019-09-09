package com.ringoid.origin.view.base.theme

import com.ringoid.base.viewmodel.SimpleViewModel

abstract class ThemedSimpleBaseActivity : ThemedBaseActivity<SimpleViewModel>() {

    override fun getVmClass() = SimpleViewModel::class.java
}
