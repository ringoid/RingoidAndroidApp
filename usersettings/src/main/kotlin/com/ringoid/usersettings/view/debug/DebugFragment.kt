package com.ringoid.usersettings.view.debug

import com.ringoid.base.view.BaseFragment
import com.ringoid.usersettings.R

class DebugFragment : BaseFragment<DebugViewModel>() {

    override fun getVmClass(): Class<DebugViewModel> = DebugViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_debug
}
