package com.ringoid.origin.usersettings.view.info

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.navigation.AppScreen
import com.ringoid.base.view.BaseFragment
import com.ringoid.domain.DomainUtil
import com.ringoid.origin.AppRes
import com.ringoid.origin.BuildConfig
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.origin.navigation.navigate
import com.ringoid.origin.usersettings.OriginR_string
import com.ringoid.origin.usersettings.R
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.copyToClipboard
import com.ringoid.utility.toast
import kotlinx.android.synthetic.main.fragment_settings_app_info.*

class SettingsAppInfoFragment : BaseFragment<SettingsAppInfoViewModel>() {

    companion object {
        internal const val TAG = "SettingsAppInfoFragment_tag"

        fun newInstance(): SettingsAppInfoFragment = SettingsAppInfoFragment()
    }

    private var developerCounter: Int = 0  // to enable developer mode

    override fun getVmClass(): Class<SettingsAppInfoViewModel> = SettingsAppInfoViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_settings_app_info

    override fun appScreen(): AppScreen = AppScreen.SETTINGS_INFO

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        item_customer_id.setLabel(spm.currentUserId() ?: "")
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fun showDebugOptions() {
            item_debug.apply {
                changeVisibility(isVisible = config.isDeveloper())
                clicks().compose(clickDebounce()).subscribe { navigate(this@SettingsAppInfoFragment, path="/debug") }
            }
            debug_underscore.changeVisibility(isVisible = config.isDeveloper())
        }

        super.onViewCreated(view, savedInstanceState)
        (toolbar as Toolbar).apply {
            setNavigationOnClickListener { activity?.onBackPressed() }
            setTitle(OriginR_string.settings_info_title)
        }

        btn_developer.clicks().compose(clickDebounce()).subscribe {
            ++developerCounter
            if (developerCounter == 7 && !config.isDeveloper()) {
                spm.enableDeveloperMode()
                showDebugOptions()
            }
        }
        item_about.apply {
            clicks().compose(clickDebounce()).subscribe {
                AboutDialog.newInstance().showNow(childFragmentManager, AboutDialog.TAG)
            }
            setLabel(BuildConfig.VERSION_NAME)
        }
        item_customer_id.clicks().compose(clickDebounce()).subscribe {
            context?.let {
                it.copyToClipboard(key = DomainUtil.CLIPBOARD_KEY_CUSTOMER_ID, value = item_customer_id.getLabel().toString())
                it.toast(OriginR_string.common_clipboard)
            }
        }
        item_email_officer.clicks().compose(clickDebounce()).subscribe {
            ExternalNavigator.emailDataProtectionOfficer(this, bodyContent = "${item_customer_id.getLabel()}...")
        }
        item_licenses.clicks().compose(clickDebounce()).subscribe { navigate(this, path = "/webpage?url=${AppRes.WEB_URL_LICENSES}") }
        item_privacy.clicks().compose(clickDebounce()).subscribe { navigate(this, path = "/webpage?url=${AppRes.WEB_URL_PRIVACY}") }
        item_terms.clicks().compose(clickDebounce()).subscribe { navigate(this, path = "/webpage?url=${AppRes.WEB_URL_TERMS}") }
        showDebugOptions()
    }
}
