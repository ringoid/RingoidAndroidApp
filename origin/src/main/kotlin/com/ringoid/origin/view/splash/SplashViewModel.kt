package com.ringoid.origin.view.splash

import android.app.Application
import android.content.Intent
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.origin.utils.ReferralUtils
import timber.log.Timber
import javax.inject.Inject

class SplashViewModel @Inject constructor(app: Application) : BaseViewModel(app) {

    // --------------------------------------------------------------------------------------------
    fun analyzeIntent(intent: Intent?) {
        val referralId = ReferralUtils.getReferralCode(intent)
        Timber.v("Referral Code: $referralId")
        spm.setReferralCode(referralId)  // save input referral code (or null)
    }
}
