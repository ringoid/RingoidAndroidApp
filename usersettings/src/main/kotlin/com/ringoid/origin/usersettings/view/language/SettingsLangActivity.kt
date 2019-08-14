package com.ringoid.origin.usersettings.view.language

import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.navigation.AppScreen
import com.ringoid.origin.view.base.BaseHostActivity

@AppNav("settings_lang")
class SettingsLangActivity : BaseHostActivity() {

    override fun appScreen(): AppScreen = AppScreen.SETTINGS_LANGUAGE
    override fun getFragmentTag(): String = SettingsLangFragment.TAG
    override fun instantiateFragment(): Fragment = SettingsLangFragment.newInstance()
}
