package com.ringoid.usersettings.view.info

import android.os.Bundle
import com.ringoid.base.deeplink.AppNav
import com.ringoid.origin.view.base.BaseHostActivity
import com.ringoid.usersettings.R

@AppNav("settings_info")
class SettingsAppInfoActivity : BaseHostActivity() {

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState ?: run {
            val fragment = SettingsAppInfoFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, fragment, SettingsAppInfoFragment.TAG)
                .commitNow()
        }
    }
}
