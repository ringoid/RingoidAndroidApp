package com.ringoid.origin.dating.app.deeplink

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.origin.utils.ReferralUtils
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class ReferralInstallListener : BroadcastReceiver() {

    @Inject lateinit var spm: ISharedPrefsManager

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        val referralId = ReferralUtils.getReferralCode(intent)
        if (!referralId.isNullOrBlank()) {
            Timber.v("Referral Code on install: $referralId")
            SentryUtil.i("Referral Code received on App install",
                         listOf("referralId" to "$referralId", "link" to intent.dataString))
        }
        spm.setReferralCode(referralId, dontOverride = !referralId.isNullOrBlank())  // save input referral code (or null)
    }
}
