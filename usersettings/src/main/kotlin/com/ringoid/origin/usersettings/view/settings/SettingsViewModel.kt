package com.ringoid.origin.usersettings.view.settings

import android.app.Application
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.user.DeleteUserProfileUseCase
import com.ringoid.domain.interactor.user.UpdateUserSettingsUseCase
import com.ringoid.domain.model.essence.user.UpdateUserSettingsEssenceUnauthorized
import com.ringoid.domain.model.user.UserSettings
import com.ringoid.origin.style.APP_THEME
import com.ringoid.origin.style.ThemeUtils
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val deleteUserProfileUseCase: DeleteUserProfileUseCase,
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase,
    app: Application) : BaseViewModel(app) {

    fun deleteAccount() {
        deleteUserProfileUseCase.source()
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnComplete { viewState.value = ViewState.CLOSE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({ Timber.i("Successfully deleted user account") }, Timber::e)
    }

    // ------------------------------------------
    fun updateUserSettingPush(isEnabled: Boolean) {
        spm.setUserSettingPushEnabled(isEnabled)
        val settings = UserSettings(push = isEnabled)
        val params = Params().put(UpdateUserSettingsEssenceUnauthorized(settings))
        updateUserSettingsUseCase.source(params = params)
            .subscribe({ DebugLogUtil.i("Successfully updated push settings: $isEnabled") }, Timber::e)
    }

    fun switchTheme() {
        val newTheme = ThemeUtils.switchTheme(spm)
        viewState.value = ViewState.DONE(APP_THEME(newTheme))
        viewState.value = ViewState.IDLE
    }
}
