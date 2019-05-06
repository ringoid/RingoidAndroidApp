package com.ringoid.base.manager.analytics

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.flurry.android.FlurryAgent
import com.google.firebase.analytics.FirebaseAnalytics
import com.ringoid.domain.manager.ISharedPrefsManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(context: Context, private val spm: ISharedPrefsManager) {

    companion object {
        private val consumedEventIds = mutableSetOf<String>()
    }

    private val firebase = FirebaseAnalytics.getInstance(context)

    fun enterUserScope() {
        consumedEventIds.clear()
    }

    fun setCurrentScreenName(activity: Activity, screenName: String = "none") {
        firebase.setCurrentScreen(activity, screenName, null)
    }

    fun setUser(spm: ISharedPrefsManager) {
        spm.currentUserId()?.let { setUserId(it) }
    }

    fun setUserId(userId: String?) {
        userId?.let {
            firebase.setUserId(it)
            FlurryAgent.setUserId(it)
        }
    }

    // --------------------------------------------------------------------------------------------
    fun hasFiredOnce(id: String): Boolean = consumedEventIds.contains(id)

    /**
     * Fire analytics event once per user session.
     */
    fun fireOnce(id: String, vararg payload: Pair<String, String>) {
        if (!hasFiredOnce(id)) {
            consumedEventIds.add(id)
            fire(id, *payload)
        }
    }

    fun fire(id: String, vararg payload: Pair<String, String>) {
        val mpayload = mutableMapOf<String, String>().apply { put("UUID", spm.getAppUid()) }
        val xpayload = Bundle().apply { putString("UUID", spm.getAppUid()) }
        payload.forEach {
            mpayload[it.first] = it.second
            xpayload.putString(it.first, it.second)
        }
        firebase.logEvent(id, xpayload)
        FlurryAgent.logEvent(id, mpayload)
    }
}
