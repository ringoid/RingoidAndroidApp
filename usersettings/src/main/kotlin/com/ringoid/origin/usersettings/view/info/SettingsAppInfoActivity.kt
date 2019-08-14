package com.ringoid.origin.usersettings.view.info

import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.navigation.AppScreen
import com.ringoid.origin.view.base.BaseHostActivity

@AppNav("settings_info")
class SettingsAppInfoActivity : BaseHostActivity() {

    override fun appScreen(): AppScreen = AppScreen.SETTINGS_INFO
    override fun getFragmentTag(): String = SettingsAppInfoFragment.TAG
    override fun instantiateFragment(): Fragment = SettingsAppInfoFragment.newInstance()
}
