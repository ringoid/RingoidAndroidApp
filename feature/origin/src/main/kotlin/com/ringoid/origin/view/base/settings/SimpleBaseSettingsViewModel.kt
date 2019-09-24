package com.ringoid.origin.view.base.settings

import android.app.Application
import com.ringoid.domain.interactor.system.PostToSlackUseCase
import com.ringoid.origin.di.DummyInjectableField
import javax.inject.Inject

open class SimpleBaseSettingsViewModel(postToSlackUseCase: PostToSlackUseCase, app: Application)
    : BaseSettingsViewModel(postToSlackUseCase, app) {

    @Inject lateinit var dummy: DummyInjectableField
}
