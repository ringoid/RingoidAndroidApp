package com.ringoid.origin.usersettings.view.push

import android.app.Application
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.user.UpdateUserSettingsUseCase
import com.ringoid.domain.model.essence.user.UpdateUserSettingsEssenceUnauthorized
import com.ringoid.domain.model.user.UserSettings
import timber.log.Timber
import javax.inject.Inject

class SettingsPushViewModel @Inject constructor(
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase,
    app: Application) : BaseViewModel(app) {

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

    private fun updateUserSettingPush(settings: UserSettings) {
        val params = Params().put(UpdateUserSettingsEssenceUnauthorized(settings))
        updateUserSettingsUseCase.source(params = params)
            .subscribe({ DebugLogUtil.i("Successfully updated push settings: $settings") }, Timber::e)
    }
}
