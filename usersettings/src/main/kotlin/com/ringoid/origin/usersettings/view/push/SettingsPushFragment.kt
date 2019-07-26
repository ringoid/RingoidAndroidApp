package com.ringoid.origin.usersettings.view.push

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.ViewState
import com.ringoid.origin.error.handleOnView
import com.ringoid.origin.usersettings.OriginR_string
import com.ringoid.origin.usersettings.R
import com.ringoid.origin.usersettings.view.base.BaseSettingsFragment
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.fragment_settings_push.*

class SettingsPushFragment : BaseSettingsFragment<SettingsPushViewModel>() {

    companion object {
        internal const val TAG = "SettingsPushFragment_tag"

        fun newInstance(): SettingsPushFragment = SettingsPushFragment()
    }

    override fun getVmClass(): Class<SettingsPushViewModel> = SettingsPushViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_settings_push

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun onIdleState() {
            pb_loading.changeVisibility(isVisible = false, soft = true)
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.IDLE -> onIdleState()
            is ViewState.LOADING -> pb_loading.changeVisibility(isVisible = true)
            is ViewState.ERROR -> newState.e.handleOnView(this, ::onIdleState)
        }
    }

    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (toolbar as Toolbar).apply {
            setNavigationOnClickListener { activity?.onBackPressed() }
            setTitle(OriginR_string.settings_push)
        }

        item_push_daily.apply {
            setChecked(spm.getUserSettingDailyPushEnabled())
            clicks().compose(clickDebounce()).subscribe { vm.updateUserSettingPushDaily(isChecked()) }
        }
        item_push_like.apply {
            setChecked(spm.getUserSettingLikesPushEnabled())
            clicks().compose(clickDebounce()).subscribe { vm.updateUserSettingPushLikes(isChecked()) }
        }
        item_push_match.apply {
            setChecked(spm.getUserSettingMatchesPushEnabled())
            clicks().compose(clickDebounce()).subscribe { vm.updateUserSettingPushMatches(isChecked()) }
        }
        item_push_message.apply {
            setChecked(spm.getUserSettingMessagesPushEnabled())
            clicks().compose(clickDebounce()).subscribe { vm.updateUserSettingPushMessages(isChecked()) }
        }

        // other
        item_suggest_improvements.clicks().compose(clickDebounce()).subscribe { openSuggestImprovementsDialog("SuggestFromNotificationsSettings") }
    }
}
