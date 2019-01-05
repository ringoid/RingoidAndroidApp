package com.ringoid.usersettings.view

import com.ringoid.base.view.BaseFragment
import com.ringoid.usersettings.R

class SettingsFragment : BaseFragment<SettingsViewModel>() {

    override fun getVmClass(): Class<SettingsViewModel> = SettingsViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_settings
}
