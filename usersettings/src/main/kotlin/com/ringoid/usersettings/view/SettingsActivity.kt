package com.ringoid.usersettings.view

import android.os.Bundle
import com.ringoid.base.deeplink.AppNav
import com.ringoid.origin.view.base.BaseHostActivity
import com.ringoid.usersettings.R

@AppNav("settings")
class SettingsActivity : BaseHostActivity() {

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState ?: run {
            val fragment = SettingsFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, fragment, SettingsFragment.TAG)
                .commitNow()
        }
    }
}
