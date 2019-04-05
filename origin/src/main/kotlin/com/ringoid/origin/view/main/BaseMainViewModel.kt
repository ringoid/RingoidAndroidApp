package com.ringoid.origin.view.main

import android.app.Application
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.exception.WrongRequestParamsClientApiException
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.push.UpdatePushTokenUseCase
import com.ringoid.domain.interactor.user.ApplyReferralCodeUseCase
import com.ringoid.domain.interactor.user.UpdateUserSettingsUseCase
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.model.essence.push.PushTokenEssenceUnauthorized
import com.ringoid.domain.model.essence.user.ReferralCodeEssenceUnauthorized
import com.ringoid.domain.model.essence.user.UpdateUserSettingsEssenceUnauthorized
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber

abstract class BaseMainViewModel(
    private val applyReferralCodeUseCase: ApplyReferralCodeUseCase,
    private val updatePushTokenUseCase: UpdatePushTokenUseCase,
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase,
    app: Application) : BaseViewModel(app) {

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onFreshStart() {
        super.onFreshStart()
        SentryUtil.setUser(spm)
        onEachAppStart()
    }

    fun onAppReOpen() {
        Bus.post(event = BusEvent.ReOpenApp)
        onEachAppStart()
    }

    private fun onEachAppStart() {
        applyReferralCodeIfAny()
        updateUserSettings()
    }

    // --------------------------------------------------------------------------------------------
    private fun applyReferralCodeIfAny() {
        spm.getReferralCode()
            ?.takeIf { !it.isNullOrBlank() }
            ?.let { referralCode ->
                applyReferralCodeUseCase.source(params = Params().put(ReferralCodeEssenceUnauthorized(referralCode)))
                    .doOnComplete { spm.setReferralCode(null) }  // drop accepted referral code
                    .autoDisposable(this)
                    .subscribe({ DebugLogUtil.i("Referral code [$referralCode] has been accepted") },
                               { Timber.e(it)
                                   when (it) {
                                       is WrongRequestParamsClientApiException -> {
                                           DebugLogUtil.w("Referral code [$referralCode] has been declined")
                                           spm.setReferralCode(null)  // drop declined referral code
                                       }
                                   }
                               })
            }
    }

    fun updatePushToken(token: String) {
        val params = Params().put(PushTokenEssenceUnauthorized(token))
        updatePushTokenUseCase.source(params = params)
            .subscribe({ DebugLogUtil.i("Successfully uploaded Firebase push token: $token") }, Timber::e)
    }

    private fun updateUserSettings() {
        val settings = app.userSettingsManager.getUserSettings()
        val params = Params().put(UpdateUserSettingsEssenceUnauthorized(settings))
        updateUserSettingsUseCase.source(params = params)
            .subscribe({ DebugLogUtil.i("Successfully updated user settings") }, Timber::e)
    }
}
