package com.ringoid.origin.view.splash

import android.app.Application
import android.content.Intent
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.log.SentryUtil
import com.ringoid.origin.utils.ReferralUtils
import timber.log.Timber
import javax.inject.Inject

class SplashViewModel @Inject constructor(app: Application) : BaseViewModel(app) {

    // --------------------------------------------------------------------------------------------
    fun analyzeIntent(intent: Intent?) {
        val referralCode = ReferralUtils.getReferralCode(intent)
        if (!referralCode.isNullOrBlank()) {
            setReferralCode(referralCode, "referralCode" to "$referralCode",
                            "link" to "${intent?.dataString}", "source" to "direct")
        }

        // Firebase dynamic link
        intent?.let {
            FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener {
                    it?.link?.let { deepLink ->
                        Timber.v("Firebase dynamic link: $deepLink")
                        val referralCode = ReferralUtils.getReferralCode(deepLink.toString())
                        if (!referralCode.isNullOrBlank()) {
                            setReferralCode(referralCode, "referralCode" to "$referralCode",
                                            "link" to "$deepLink", "source" to "firebase")
                        }
                    }
                }
                .addOnFailureListener { Timber.e("Firebase has failed to handle dynamic link") }
        }
    }

    // ------------------------------------------
    private fun setReferralCode(referralCode: String, vararg extras: Pair<String, String>) {
        if (referralCode.isNotBlank()) {
            Timber.v("Referral Code on link open: $referralCode")
            SentryUtil.i("Referral Code received on link open", extras.toList())
        }
        spm.setReferralCode(referralCode)  // save input referral code (or null)
    }
}
