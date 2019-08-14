package com.ringoid.origin.usersettings.view.debug

import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.navigation.AppScreen
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.origin.view.base.BaseHostActivity

@AppNav("debug") @DebugOnly
class DebugActivity : BaseHostActivity() {

    override fun appScreen(): AppScreen = AppScreen.SETTINGS_DEBUG
    override fun getFragmentTag(): String = DebugFragment.TAG
    override fun instantiateFragment(): Fragment = DebugFragment.newInstance()
}
