package com.ringoid.origin.view.splash

import android.app.Application
import android.content.Intent
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.log.SentryUtil
import com.ringoid.origin.error.DynamicLinkNotExistsException
import com.ringoid.origin.utils.ReferralUtils
import timber.log.Timber
import javax.inject.Inject

class SplashViewModel @Inject constructor(app: Application) : BaseViewModel(app) {

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onFreshStart() {
        super.onFreshStart()
        DebugLogUtil.v("Fresh start App, init logs")
        SentryUtil.breadcrumb("App Version", "code" to "${BuildConfig.VERSION_CODE}", "version" to "${BuildConfig.VERSION_NAME}")
    }

    override fun onStart() {
        super.onStart()
        spm.dropFirstAppLaunch()
    }

    // --------------------------------------------------------------------------------------------
    fun analyzeIntent(intent: Intent?) {
        fun processDynamicLinkDirect(intent: Intent, isFirstAppLaunch: String, error: Throwable? = null) {
            val referralCode = ReferralUtils.getReferralCode(intent.data)
            if (!referralCode.isNullOrBlank()) {
                Timber.i("Obtained referral code manually: $referralCode")
                setReferralCode(
                    referralCode, "referralCode" to "$referralCode",
                    "deeplink" to intent.dataString,
                    "firebaseError" to "${error?.javaClass}",
                    "firebaseErrorMsg" to "${error?.message}",
                    "launch" to isFirstAppLaunch,
                    "source" to "direct")
            }
        }

        if (intent == null) {
            return  // nothing to analyze
        }
        /**
         * Need to keep this value 'isFirstAppLaunch' while [FirebaseDynamicLinks.getDynamicLink]
         * is running on a background thread, because the value will be dropped in [onStart].
         */
        val isFirstAppLaunch = if (spm.isFirstAppLaunch()) "install" else "re-open"

        // Firebase dynamic link
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener { data: PendingDynamicLinkData? ->
                data?.let {
                    it.link?.let { deepLink ->
                        Timber.i("Firebase dynamic link: $deepLink")
                        val referralCode = ReferralUtils.getReferralCode(deepLink)
                        if (!referralCode.isNullOrBlank()) {
                            setReferralCode(
                                referralCode, "referralCode" to "$referralCode",
                                "deeplink" to "$deepLink",
                                "clickedTs" to "${it.clickTimestamp}",
                                "launch" to isFirstAppLaunch,
                                "source" to "firebase"
                            )
                        }
                    }
                }
                ?: run {
                    Timber.i("No dynamic link has been found in Firebase")
                    // fallback to extract deep link from Intent manually
                    processDynamicLinkDirect(intent, isFirstAppLaunch, error = DynamicLinkNotExistsException(intent.data))
                }
            }
            .addOnFailureListener {
                Timber.e(it, "Firebase has failed to handle dynamic link")
                // fallback to extract deep link from Intent manually
                processDynamicLinkDirect(intent, isFirstAppLaunch, error = it)
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
