package com.ringoid.base.view

import com.ringoid.base.viewmodel.SimpleViewModel

abstract class SimpleBaseDialogFragment : BaseDialogFragment<SimpleViewModel>() {

    override fun getVmClass() = SimpleViewModel::class.java
}
