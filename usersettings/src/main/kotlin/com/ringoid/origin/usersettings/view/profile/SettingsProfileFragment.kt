package com.ringoid.origin.usersettings.view.profile

import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.usersettings.R

class SettingsProfileFragment : BaseFragment<SettingsProfileViewModel>() {

    companion object {
        internal const val TAG = "SettingsProfileFragment_tag"

        fun newInstance(): SettingsProfileFragment = SettingsProfileFragment()
    }

    override fun getVmClass(): Class<SettingsProfileViewModel> = SettingsProfileViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_settings_profile
}
