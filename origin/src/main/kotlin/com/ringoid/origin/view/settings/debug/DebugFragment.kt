package com.ringoid.origin.view.settings.debug

import com.ringoid.base.view.BaseFragment

class DebugFragment : BaseFragment<DebugViewModel>() {

    override fun getVmClass(): Class<DebugViewModel> = DebugViewModel::class.java

    override fun getLayoutId(): Int = 0
}
