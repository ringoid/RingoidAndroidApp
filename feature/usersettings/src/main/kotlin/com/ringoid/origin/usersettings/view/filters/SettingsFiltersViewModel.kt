package com.ringoid.origin.usersettings.view.filters

import android.app.Application
import com.ringoid.domain.interactor.system.PostToSlackUseCase
import com.ringoid.origin.view.base.settings.BaseSettingsViewModel
import javax.inject.Inject

class SettingsFiltersViewModel @Inject constructor(postToSlackUseCase: PostToSlackUseCase, app: Application)
    : BaseSettingsViewModel(postToSlackUseCase, app)
