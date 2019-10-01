package com.ringoid.origin.usersettings.view.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.navigation.AppScreen
import com.ringoid.origin.view.base.BaseHostActivity
import com.ringoid.utility.hideKeyboard

@AppNav("settings_profile")
class SettingsProfileActivity : BaseHostActivity() {

    private var focus: String? = null
    private var onboarding: Boolean = false

    override fun appScreen(): AppScreen = AppScreen.SETTINGS_PROFILE
    override fun getFragmentTag(): String = SettingsProfileFragment.TAG
    override fun instantiateFragment(): Fragment = SettingsProfileFragment.newInstance(focus, onboarding)

    override fun onCreate(savedInstanceState: Bundle?) {
        focus = intent.extras?.getString("focus")
        onboarding = intent.extras?.getString("onboarding")?.toBoolean() ?: false
        super.onCreate(savedInstanceState)
    }

    override fun onBackPressed() {
        hideKeyboard()
        super.onBackPressed()
    }
}
