package com.ringoid.origin.usersettings.view.filters

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.origin.usersettings.OriginR_string
import com.ringoid.origin.usersettings.R
import com.ringoid.origin.usersettings.view.base.BaseSettingsFragment
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

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (toolbar as Toolbar).apply {
            setNavigationOnClickListener { activity?.onBackPressed() }
            setTitle(OriginR_string.settings_filters)
        }

        item_suggest_improvements.clicks().compose(clickDebounce()).subscribe { openSuggestImprovementsDialog("SuggestFromFiltersSettings") }

        if (savedInstanceState == null) {
            childFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, FiltersFragment.newInstance(), FiltersFragment.TAG)
                .commitNowAllowingStateLoss()
        }
    }
}
