package com.ringoid.origin.usersettings.view.settings

import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.navigation.AppScreen
import com.ringoid.debug.DebugLogUtil
import com.ringoid.origin.view.base.BaseHostActivity

@AppNav("settings")
class SettingsActivity : BaseHostActivity() {

    override fun appScreen(): AppScreen = AppScreen.SETTINGS
    override fun getFragmentTag(): String = SettingsFragment.TAG
    override fun instantiateFragment(): Fragment = SettingsFragment.newInstance()

    override fun onDestroy() {
        super.onDestroy()
        DebugLogUtil.clear()  // clear debug logs when back from Settings screen
    }
}
