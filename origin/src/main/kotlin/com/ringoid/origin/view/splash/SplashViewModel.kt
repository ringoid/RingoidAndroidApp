package com.ringoid.origin.view.splash

import android.app.Application
import android.content.Intent
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.log.SentryUtil
import com.ringoid.origin.utils.ReferralUtils
import timber.log.Timber
import javax.inject.Inject

class SplashViewModel @Inject constructor(app: Application) : BaseViewModel(app) {

    // --------------------------------------------------------------------------------------------
    fun analyzeIntent(intent: Intent?) {
        val referralId = ReferralUtils.getReferralCode(intent)
        if (!referralId.isNullOrBlank()) {
            Timber.v("Referral Code on link open: $referralId")
            SentryUtil.i("Referral Code received on link open",
                         listOf("referralId" to "$referralId", "link" to "${intent?.dataString}"))
        }
        spm.setReferralCode(referralId)  // save input referral code (or null)
    }
}
