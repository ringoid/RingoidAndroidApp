package com.ringoid.origin.usersettings.view.push

import android.app.Application
import com.ringoid.base.view.ViewState
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.system.PostToSlackUseCase
import com.ringoid.domain.interactor.user.UpdateUserSettingsUseCase
import com.ringoid.domain.model.essence.user.UpdateUserSettingsEssenceUnauthorized
import com.ringoid.domain.model.user.UserSettings
import com.ringoid.origin.usersettings.view.base.BaseSettingsViewModel
import com.uber.autodispose.lifecycle.autoDisposable
import javax.inject.Inject

class SettingsPushViewModel @Inject constructor(
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase,
    postToSlackUseCase: PostToSlackUseCase, app: Application)
    : BaseSettingsViewModel(postToSlackUseCase, app) {

    fun updateUserSettingPushDaily(isEnabled: Boolean) {
        spm.setUserSettingDailyPushEnabled(isEnabled)
        updateUserSettingPush(UserSettings(push = isEnabled))
    }

    fun updateUserSettingPushLikes(isEnabled: Boolean) {
        spm.setUserSettingLikesPushEnabled(isEnabled)
        updateUserSettingPush(UserSettings(pushLikes = isEnabled))
    }

    fun updateUserSettingPushMatches(isEnabled: Boolean) {
        spm.setUserSettingMatchesPushEnabled(isEnabled)
        updateUserSettingPush(UserSettings(pushMatches = isEnabled))
    }

    fun updateUserSettingPushMessages(isEnabled: Boolean) {
        spm.setUserSettingMessagesPushEnabled(isEnabled)
        updateUserSettingPush(UserSettings(pushMessages = isEnabled))
    }

    fun updateUserSettingPushVibration(isEnabled: Boolean) {
        spm.setUserSettingVibrationPushEnabled(isEnabled)
        updateUserSettingPush(UserSettings(pushVibration = isEnabled))
    }

    private fun updateUserSettingPush(settings: UserSettings) {
        val params = Params().put(UpdateUserSettingsEssenceUnauthorized(settings))
        updateUserSettingsUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnComplete { viewState.value = ViewState.IDLE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({ DebugLogUtil.i("Successfully updated push settings: $settings") }, DebugLogUtil::e)
    }
}
