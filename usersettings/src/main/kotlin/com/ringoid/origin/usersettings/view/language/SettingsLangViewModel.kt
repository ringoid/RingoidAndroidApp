package com.ringoid.origin.usersettings.view.language

import android.app.Application
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.user.UpdateUserSettingsUseCase
import com.ringoid.domain.model.essence.user.UpdateUserSettingsEssenceUnauthorized
import com.ringoid.domain.model.user.UserSettings
import com.ringoid.utility.manager.LocaleManager
import timber.log.Timber
import javax.inject.Inject

class SettingsLangViewModel @Inject constructor(
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase,
    app: Application) : BaseViewModel(app) {

    fun updateUserSettingLocale() {
        val settings = UserSettings(locale = app.localeManager.getLang())
        val params = Params().put(UpdateUserSettingsEssenceUnauthorized(settings))
        updateUserSettingsUseCase.source(params = params)
            .subscribe({ DebugLogUtil.i("Successfully updated locale: ${settings.locale}") }, Timber::e)
    }
}
