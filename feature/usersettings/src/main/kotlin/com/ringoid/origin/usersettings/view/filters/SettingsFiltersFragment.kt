package com.ringoid.origin.usersettings.view.filters

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding3.appcompat.itemClicks
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.navigation.AppScreen
import com.ringoid.origin.usersettings.OriginR_id
import com.ringoid.origin.usersettings.OriginR_menu
import com.ringoid.origin.usersettings.OriginR_string
import com.ringoid.origin.usersettings.R
import com.ringoid.origin.view.base.settings.BaseSettingsFragment
import com.ringoid.origin.view.filters.BaseFiltersFragment
import com.ringoid.origin.view.filters.FiltersFragment
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.fragment_settings_filters.*

class SettingsFiltersFragment : BaseSettingsFragment<SettingsFiltersViewModel>() {

    companion object {
        internal const val TAG = "SettingsFiltersFragment_tag"

        fun newInstance(): SettingsFiltersFragment = SettingsFiltersFragment()
    }

    override fun getVmClass(): Class<SettingsFiltersViewModel> = SettingsFiltersViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_settings_filters

    override fun appScreen(): AppScreen = AppScreen.SETTINGS_FILTERS

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (toolbar as Toolbar).apply {
            setTitle(OriginR_string.settings_filters)
            inflateMenu(OriginR_menu.menu_done)
            itemClicks().compose(clickDebounce()).subscribe {
                when (it.itemId) {
                    OriginR_id.menu_done -> activity?.onBackPressed()
                }
            }
            navigationClicks().compose(clickDebounce()).subscribe { activity?.onBackPressed() }
        }

        item_suggest_improvements.clicks().compose(clickDebounce()).subscribe { openSuggestImprovementsDialog("SuggestFromFiltersSettings") }

        if (savedInstanceState == null) {
            childFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, FiltersFragment.newInstance(), BaseFiltersFragment.TAG)
                .commitNowAllowingStateLoss()
        }
    }
}
