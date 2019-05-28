package com.ringoid.origin.usersettings.view.settings

import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.origin.view.base.BaseHostActivity

@AppNav("settings")
class SettingsActivity : BaseHostActivity() {

    override fun getFragmentTag(): String = SettingsFragment.TAG
    override fun instantiateFragment(): Fragment = SettingsFragment.newInstance()

    override fun onDestroy() {
        super.onDestroy()
        DebugLogUtil.clear()  // clear debug logs when back from Settings screen
    }
}
