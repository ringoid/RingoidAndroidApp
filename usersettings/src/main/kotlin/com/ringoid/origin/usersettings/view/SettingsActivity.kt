package com.ringoid.origin.usersettings.view

import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.origin.view.base.BaseHostActivity

@AppNav("settings")
class SettingsActivity : BaseHostActivity() {

    override fun getFragmentTag(): String = SettingsFragment.TAG
    override fun instantiateFragment(): Fragment = SettingsFragment.newInstance()
}
