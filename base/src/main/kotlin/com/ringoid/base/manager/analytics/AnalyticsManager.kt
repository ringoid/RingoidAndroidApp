package com.ringoid.base.manager.analytics

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.ringoid.domain.manager.ISharedPrefsManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(context: Context, private val spm: ISharedPrefsManager) {

    private val firebase = FirebaseAnalytics.getInstance(context)

    fun setCurrentScreenName(activity: Activity, screenName: String = "none") {
        firebase.setCurrentScreen(activity, screenName, null)
    }

    fun setUserId(userId: String?) {
        firebase.setUserId(userId)
    }

    fun fire(id: String, vararg payload: Pair<String, String>) {
        val xpayload = Bundle()
            .apply {
                putString("UUID", spm.getAppUid())
                payload.forEach { putString(it.first, it.second) }
            }
        firebase.logEvent(id, xpayload)
    }
}
