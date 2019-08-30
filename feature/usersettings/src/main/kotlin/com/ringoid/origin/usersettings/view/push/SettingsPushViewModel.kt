package com.ringoid.origin.usersettings.view.push

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.view.ViewState
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.system.PostToSlackUseCase
import com.ringoid.domain.interactor.user.UpdateUserSettingsUseCase
import com.ringoid.domain.misc.PushSettingsRaw
import com.ringoid.domain.model.essence.user.UpdateUserSettingsEssenceUnauthorized
import com.ringoid.domain.model.user.UserSettings
import com.ringoid.origin.usersettings.view.base.BaseSettingsViewModel
import com.uber.autodispose.lifecycle.autoDisposable
import javax.inject.Inject

class SettingsPushViewModel @Inject constructor(
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase,
    postToSlackUseCase: PostToSlackUseCase, app: Application)
    : BaseSettingsViewModel(postToSlackUseCase, app) {

    private val pushSettings by lazy { MutableLiveData<PushSettingsRaw>() }
    internal fun pushSettings(): LiveData<PushSettingsRaw> = pushSettings

    private lateinit var properties: PushSettingsRaw

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        properties = spm.getUserPushSettings()
        pushSettings.value = properties  // assign initial values to views
    }

    // --------------------------------------------------------------------------------------------
    fun updateUserSettingPushDaily(isEnabled: Boolean) {
        properties.push = isEnabled
        updateUserSettingPush(UserSettings(push = isEnabled))
    }

    fun updateUserSettingPushLikes(isEnabled: Boolean) {
        properties.pushLikes = isEnabled
        updateUserSettingPush(UserSettings(pushLikes = isEnabled))
    }

    fun updateUserSettingPushMatches(isEnabled: Boolean) {
        properties.pushMatches = isEnabled
        updateUserSettingPush(UserSettings(pushMatches = isEnabled))
    }

    fun updateUserSettingPushMessages(isEnabled: Boolean) {
        properties.pushMessages = isEnabled
        updateUserSettingPush(UserSettings(pushMessages = isEnabled))
    }

    fun updateUserSettingPushVibration(isEnabled: Boolean) {
        properties.pushVibration = isEnabled
        updateUserSettingPush(UserSettings(pushVibration = isEnabled))
    }

    private fun updateUserSettingPush(settings: UserSettings) {
        val params = Params().put(UpdateUserSettingsEssenceUnauthorized(settings))
        updateUserSettingsUseCase.source(params = params)
            .doOnSubscribe {
                viewState.value = ViewState.LOADING  // updated user push settings progress
                spm.setUserPushSettings(settingsRaw = properties)
            }
            .doOnComplete { viewState.value = ViewState.IDLE }  // updated user push settings success
            .doOnError { viewState.value = ViewState.ERROR(it) }  // updated user push settings failed
            .autoDisposable(this)
            .subscribe({ DebugLogUtil.i("Successfully updated push settings: $settings") }, DebugLogUtil::e)
    }
}
