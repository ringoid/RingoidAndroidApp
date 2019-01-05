package com.ringoid.usersettings.view.info

import com.ringoid.base.view.BaseFragment
import com.ringoid.usersettings.R

class SettingsAppInfoFragment : BaseFragment<SettingsAppInfoViewModel>() {

    override fun getVmClass(): Class<SettingsAppInfoViewModel> = SettingsAppInfoViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_settings_app_info
}
