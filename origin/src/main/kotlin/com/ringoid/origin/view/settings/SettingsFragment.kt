package com.ringoid.origin.view.settings

import com.ringoid.base.view.BaseFragment

class SettingsFragment : BaseFragment<SettingsViewModel>() {

    override fun getVmClass(): Class<SettingsViewModel> = SettingsViewModel::class.java

    override fun getLayoutId(): Int = 0
}
