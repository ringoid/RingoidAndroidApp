package com.ringoid.origin.dating.app.deeplink

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.origin.utils.ReferralUtils
import com.ringoid.report.log.Report
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class ReferralInstallListener : BroadcastReceiver() {
    
    @Inject lateinit var spm: ISharedPrefsManager

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        val referralId = ReferralUtils.getReferralCode(intent.data)
        if (!referralId.isNullOrBlank()) {
            Timber.v("Referral Code on install: $referralId")
            Report.i("Referral Code received on App install",
                     listOf("referralId" to "$referralId", "link" to intent.dataString))
        }
//        Report.i("Referral Install callback",
//                 listOf("referralId" to "$referralId", "data" to "$intent"))
        spm.setReferralCode(referralId, dontOverride = !referralId.isNullOrBlank())  // save input referral code (or null)
    }
}
