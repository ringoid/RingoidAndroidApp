package com.ringoid.origin.view.settings.info

import com.ringoid.base.view.BaseFragment

class SettingsAppInfoFragment : BaseFragment<SettingsAppInfoViewModel>() {

    override fun getVmClass(): Class<SettingsAppInfoViewModel> = SettingsAppInfoViewModel::class.java

    override fun getLayoutId(): Int = 0
}
