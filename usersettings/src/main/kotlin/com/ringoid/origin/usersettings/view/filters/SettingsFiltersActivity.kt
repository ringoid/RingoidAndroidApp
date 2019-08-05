package com.ringoid.origin.usersettings.view.filters

import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.origin.view.base.BaseHostActivity

@AppNav("settings_filters")
class SettingsFiltersActivity : BaseHostActivity() {

    override fun getFragmentTag(): String = SettingsFiltersFragment.TAG
    override fun instantiateFragment(): Fragment = SettingsFiltersFragment.newInstance()
}
