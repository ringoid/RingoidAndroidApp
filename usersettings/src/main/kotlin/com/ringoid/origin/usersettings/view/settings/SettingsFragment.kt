package com.ringoid.origin.usersettings.view.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.ViewState
import com.ringoid.origin.error.handleOnView
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.origin.navigation.RequestCode
import com.ringoid.origin.navigation.logout
import com.ringoid.origin.navigation.navigate
import com.ringoid.origin.style.APP_THEME
import com.ringoid.origin.usersettings.OriginR_string
import com.ringoid.origin.usersettings.R
import com.ringoid.origin.usersettings.view.base.BaseSettingsFragment
import com.ringoid.origin.view.dialog.BigEditTextDialog
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : BaseSettingsFragment<SettingsViewModel>() {

    companion object {
        internal const val TAG = "SettingsFragment_tag"

        fun newInstance(): SettingsFragment = SettingsFragment()
    }

    override fun getVmClass(): Class<SettingsViewModel> = SettingsViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_settings

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun onIdleState() {
            pb_settings.changeVisibility(isVisible = false, soft = true)
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.CLOSE -> logout(this)
            is ViewState.DONE -> {
                when (newState.residual) {
                    is APP_THEME -> activity?.recreate()
                }
            }
            is ViewState.LOADING -> pb_settings.changeVisibility(isVisible = true)
            is ViewState.ERROR -> newState.e.handleOnView(this, ::onIdleState)
            else -> { /* no-op */ }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RequestCode.RC_SETTINGS_LANG -> activity?.recreate()
            }
        }
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (toolbar as Toolbar).apply {
            setNavigationOnClickListener { activity?.onBackPressed() }
            setTitle(OriginR_string.settings_title)
        }

        item_delete_account.clicks().compose(clickDebounce()).subscribe { openDeleteAccountDialog() }
        item_language.apply {
            clicks().compose(clickDebounce()).subscribe { navigate(this@SettingsFragment, path = "/settings_lang", rc = RequestCode.RC_SETTINGS_LANG) }
//            setLabel(LocaleUtils.getLangById(context, app?.localeManager?.getLang() ?: LocaleManager.LANG_RU))
        }
        item_legal.clicks().compose(clickDebounce()).subscribe { navigate(this, path = "/settings_info") }
        item_profile.clicks().compose(clickDebounce()).subscribe { navigate(this, path = "/settings_profile") }
        item_push.clicks().compose(clickDebounce()).subscribe { navigate(this, path = "/settings_push") }
        item_support.clicks().compose(clickDebounce()).subscribe {
            ExternalNavigator.emailSupportTeam(this, "",
                "id" to "${spm.currentUserId()}",
                "request" to "${cloudDebug.get("request")}",
                "response" to "${cloudDebug.get("result")}",
                "lastActionTime" to "${cloudDebug.get("lastActionTime")}")
        }
        item_suggest_improvements.clicks().compose(clickDebounce()).subscribe { openSuggestImprovementsDialog("SuggestFromSettings") }
//        item_theme.apply {
//            setChecked(!ThemeUtils.isDefaultTheme(spm))
//            clicks().compose(clickDebounce()).subscribe { vm.switchTheme() }
//        }
    }

    // --------------------------------------------------------------------------------------------
    override fun onDone(text: String, tag: String?) {
        super.onDone(text, tag)
        if (tag == "DeleteAccount") vm.deleteAccount()
    }

    private fun openDeleteAccountDialog() {
        BigEditTextDialog.newInstance(titleResId = OriginR_string.settings_account_delete_dialog_title,
            subtitleResId = OriginR_string.common_uncancellable,
            descriptionResId = OriginR_string.suggest_improvements_description_account_delete,
            btnPositiveResId = OriginR_string.button_delete,
            input = spm.getBigEditText(), tag = "DeleteAccount")
            .show(childFragmentManager, BigEditTextDialog.TAG)
    }
}
