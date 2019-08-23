package com.ringoid.origin.usersettings.view.push

import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.navigation.AppScreen
import com.ringoid.origin.view.base.BaseHostActivity

@AppNav("settings_push")
class SettingsPushActivity : BaseHostActivity() {

    override fun appScreen(): AppScreen = AppScreen.SETTINGS_PUSH
    override fun getFragmentTag(): String = SettingsPushFragment.TAG
    override fun instantiateFragment(): Fragment = SettingsPushFragment.newInstance()
}
