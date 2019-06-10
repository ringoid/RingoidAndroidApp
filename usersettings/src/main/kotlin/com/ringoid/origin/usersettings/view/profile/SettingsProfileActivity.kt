package com.ringoid.origin.usersettings.view.profile

import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.origin.view.base.BaseHostActivity

@AppNav("settings_profile")
class SettingsProfileActivity : BaseHostActivity() {

    override fun getFragmentTag(): String = SettingsProfileFragment.TAG
    override fun instantiateFragment(): Fragment = SettingsProfileFragment.newInstance()
}
