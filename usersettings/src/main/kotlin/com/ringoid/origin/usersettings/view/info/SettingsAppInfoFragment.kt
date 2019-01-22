package com.ringoid.origin.usersettings.view.info

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.AppRes
import com.ringoid.origin.BuildConfig
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.origin.navigation.navigate
import com.ringoid.origin.usersettings.OriginR_string
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.usersettings.R
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.fragment_settings_app_info.*

class SettingsAppInfoFragment : BaseFragment<SettingsAppInfoViewModel>() {

    companion object {
        internal const val TAG = "SettingsAppInfoFragment_tag"

        fun newInstance(): SettingsAppInfoFragment = SettingsAppInfoFragment()
    }

    override fun getVmClass(): Class<SettingsAppInfoViewModel> = SettingsAppInfoViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_settings_app_info

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        tv_customer_id.text = vm.spm.currentUserId() ?: ""
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (toolbar as Toolbar).apply {
            setNavigationOnClickListener { activity?.onBackPressed() }
            setTitle(OriginR_string.settings_info_title)
        }

        item_about.apply {
            clicks().compose(clickDebounce()).subscribe {
                Dialogs.showTextDialog(
                    activity, titleResId = OriginR_string.settings_info_about_dialog_title,
                    descriptionResId = OriginR_string.settings_info_about_dialog_description
                )
            }
            setLabel(BuildConfig.VERSION_NAME)
        }
        item_email_officer.clicks().compose(clickDebounce()).subscribe {
            val subject = String.format(AppRes.EMAIL_OFFICER_MAIL_SUBJECT, tv_customer_id.text)
            ExternalNavigator.openEmailComposer(this, email = "data.protection@ringoid.com", subject = subject)
        }
        item_licenses.clicks().compose(clickDebounce()).subscribe { navigate(this, path = "/webpage?url=${AppRes.WEB_URL_LICENSES}") }
        item_privacy.clicks().compose(clickDebounce()).subscribe { navigate(this, path = "/webpage?url=${AppRes.WEB_URL_PRIVACY}") }
        item_terms.clicks().compose(clickDebounce()).subscribe { navigate(this, path = "/webpage?url=${AppRes.WEB_URL_TERMS}") }
    }
}
