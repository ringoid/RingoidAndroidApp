package com.ringoid.origin.usersettings.view.filters

import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.origin.view.base.BaseHostActivity
import com.ringoid.origin.view.filters.IFiltersHost

@AppNav("settings_filters")
class SettingsFiltersActivity : BaseHostActivity(), IFiltersHost {

    override fun getFragmentTag(): String = SettingsFiltersFragment.TAG
    override fun instantiateFragment(): Fragment = SettingsFiltersFragment.newInstance()

    override fun onBackPressed() {
        super.onBackPressed()
        if (filtersChanged) {
            filtersChanged = false
            Bus.post(BusEvent.FiltersChangesInSettings)
        }
    }

    // --------------------------------------------------------------------------------------------
    private var filtersChanged: Boolean = false

    override fun onFiltersChange() {
        filtersChanged = true
    }
}
