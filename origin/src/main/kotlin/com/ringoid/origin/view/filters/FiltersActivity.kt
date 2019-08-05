package com.ringoid.origin.view.filters

import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.origin.view.base.BaseHostActivity

@AppNav("settings_filters")
class FiltersActivity : BaseHostActivity() {

    override fun getFragmentTag(): String = FiltersFragment.TAG
    override fun instantiateFragment(): Fragment = FiltersFragment.newInstance()
}
