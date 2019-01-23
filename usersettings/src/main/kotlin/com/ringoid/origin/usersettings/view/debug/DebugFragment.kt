package com.ringoid.origin.usersettings.view.debug

import com.ringoid.base.view.BaseFragment
import com.ringoid.usersettings.R

class DebugFragment : BaseFragment<DebugViewModel>() {

    companion object {
        const val TAG = "DebugFragment_tag"

        fun newInstance(): DebugFragment = DebugFragment()
    }

    override fun getVmClass(): Class<DebugViewModel> = DebugViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_debug
}
