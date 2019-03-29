package com.ringoid.origin.view.splash

import android.app.Application
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.push.UpdatePushTokenUseCase
import com.ringoid.domain.model.essence.push.PushTokenEssenceUnauthorized
import timber.log.Timber
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    private val updatePushTokenUseCase: UpdatePushTokenUseCase, app: Application) : BaseViewModel(app) {

    // --------------------------------------------------------------------------------------------
    fun updatePushToken(token: String) {
        val params = Params().put(PushTokenEssenceUnauthorized(token))
        updatePushTokenUseCase.source(params = params)
            .subscribe({ DebugLogUtil.i("Successfully uploaded Firebase push token: $token") }, Timber::e)
    }
}
