package com.ringoid.base.manager

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Singleton

@Singleton
class AnalyticsManager(context: Context) {

    private val firebase = FirebaseAnalytics.getInstance(context)

    fun setCurrentScreenName(activity: Activity, screenName: String = "none") {
        firebase.setCurrentScreen(activity, screenName, null)
    }

    fun setUserId(userId: String?) {
        firebase.setUserId(userId)
    }

    fun fire(id: String, payload: Bundle? = null) {
        firebase.logEvent(id, payload)
    }
}
