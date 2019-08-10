package com.ringoid.origin.usersettings.view.debug

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.navigation.AppScreen
import com.ringoid.base.view.BaseFragment
import com.ringoid.base.view.ViewState
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.origin.error.handleOnView
import com.ringoid.origin.usersettings.OriginR_string
import com.ringoid.origin.usersettings.R
import com.ringoid.utility.*
import kotlinx.android.synthetic.main.fragment_debug.*
import kotlinx.android.synthetic.main.fragment_debug.view.*
import timber.log.Timber

@DebugOnly
class DebugFragment : BaseFragment<DebugViewModel>() {

    companion object {
        const val TAG = "DebugFragment_tag"

        fun newInstance(): DebugFragment = DebugFragment()
    }

    override fun getVmClass(): Class<DebugViewModel> = DebugViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_debug

    override fun appScreen(): AppScreen = AppScreen.SETTINGS_DEBUG

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun onIdleState() {
            pb_debug.changeVisibility(isVisible = false, soft = true)
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.IDLE -> onIdleState()
            is ViewState.DONE -> { snackbar(view, "Success!") ; onIdleState() }
            is ViewState.LOADING -> pb_debug.changeVisibility(isVisible = true)
            is ViewState.ERROR -> newState.e.handleOnView(this, ::onIdleState)
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (toolbar as Toolbar).apply {
            setNavigationOnClickListener { activity?.onBackPressed() }
            setTitle(OriginR_string.debug_title)
        }

        item_error_http.clicks().compose(clickDebounce()).subscribe { vm.requestWithNotSuccessResponse() }
        item_error_http_404.clicks().compose(clickDebounce()).subscribe { vm.requestWith404Response() }
        item_error_token.clicks().compose(clickDebounce()).subscribe { vm.requestWithInvalidAccessToken() }
        item_error_token_expired.clicks().compose(clickDebounce()).subscribe { vm.requestWithExpiredAccessToken() }
        item_error_app_version.clicks().compose(clickDebounce()).subscribe { vm.requestWithStaledAppVersion() }
        item_error_server.clicks().compose(clickDebounce()).subscribe { vm.requestWithServerError() }
        item_error_commit_actions_fail_all_attempts.clicks().compose(clickDebounce()).subscribe { vm.requestWithCommitActionsFailAllRetries() }
        item_error_request_fail_all_attempts.clicks().compose(clickDebounce()).subscribe { vm.requestWithFailAllRetries() }
        item_error_request_n_fail_attempts.clicks().compose(clickDebounce()).subscribe { vm.requestWithFailNTimesBeforeSuccess(n = 3) }
        item_error_request_params.clicks().compose(clickDebounce()).subscribe { vm.requestWithWrongParams() }
        item_error_request_repeat_after_delay.clicks().compose(clickDebounce()).subscribe { vm.requestWithNeedToRepeatAfterDelay(delay = 5000L) }
        item_error_timeout.clicks().compose(clickDebounce()).subscribe { vm.requestWithTimeOutResponse() }
        item_last_request.apply {
            setText("Method: ${cloudDebug.get("request")}")
            setLabel("Resolution: ${cloudDebug.get("resolution")}")
        }
        item_debug_log.apply {
            setChecked(spm.isDebugLogEnabled())
            clicks().compose(clickDebounce()).subscribe { spm.switchDebugLogEnabled() }
        }
        item_developer_mode.apply {
            changeVisibility(isVisible = !BuildConfig.IS_STAGING)  // only visible in Production builds
            setChecked(spm.isDeveloperModeEnabled())
            clicks().compose(clickDebounce()).subscribe { spm.switchDeveloperMode() }
        }
        item_screen_info.apply {
            clicks().compose(clickDebounce()).subscribe {
                context?.let {
                    it.copyToClipboard(key = DomainUtil.CLIPBOARD_KEY_DEBUG, value = item_screen_info.getLabel().toString())
                    it.toast(OriginR_string.common_clipboard)
                }
            }
            setLabel("Density: ${activity?.getScreenDensity()}, SW: ${activity?.getSmallestWidth()}, " +
                     "W: ${activity?.getScreenWidthDp()} dp [${activity?.getScreenWidth()} px], " +
                     "H: ${activity?.getScreenHeightDp()} dp [${activity?.getScreenHeight()} px]")
        }
        btn_clear_debug_log.clicks().compose(clickDebounce()).subscribe { DebugLogUtil.clear() }
        btn_copy_debug_log.clicks().compose(clickDebounce()).subscribe {
            DebugLogUtil.getDebugLog()
                ?.map { it.joinToString("\n", transform = { it.log() }) }
                ?.subscribe({ log ->
                    context?.let {
                        it.copyToClipboard(key = DomainUtil.CLIPBOARD_KEY_DEBUG, value = log)
                        it.toast(OriginR_string.common_clipboard)
                    }
                }, Timber::e)
        }
    }
}
