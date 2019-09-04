package com.ringoid.analytics

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.facebook.appevents.AppEventsLogger
import com.flurry.android.Constants
import com.flurry.android.FlurryAgent
import com.google.firebase.analytics.FirebaseAnalytics
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.misc.Gender
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(context: Context, private val spm: ISharedPrefsManager) {

    companion object {
        private const val SP_KEY_ANALYTICS_CACHE = "sp_key_analytics_cache"

        private val consumedEventIds = mutableSetOf<String>()
    }

    private val facebook: AppEventsLogger = AppEventsLogger.newLogger(context)
    private val firebase = FirebaseAnalytics.getInstance(context)

    fun enterUserScope() {
        consumedEventIds.clear()
    }

    fun exitUserScope(spm: ISharedPrefsManager) {
        consumedEventIds.clear()
        spm.deleteByKey(SP_KEY_ANALYTICS_CACHE)
    }

    fun setCurrentScreenName(activity: Activity, screenName: String = "none") {
        firebase.setCurrentScreen(activity, screenName, null)
    }

    fun setUser(spm: ISharedPrefsManager) {
        with (spm) {
            currentUserId()?.let { setUserId(it) }
            setUserGender(currentUserGender())
            if (hasUserYearOfBirth()) {
                setUserAge(currentUserYearOfBirth())
            }
        }
    }

    private fun setUserId(userId: String?) {
        userId?.let {
            firebase.setUserId(it)
            FlurryAgent.setUserId(it)
        }
    }

    private fun setUserAge(age: Int) {
        firebase.setUserProperty("age", "$age")
        FlurryAgent.setAge(age)
    }

    private fun setUserGender(gender: Gender) {
        val g = when (gender) {
            Gender.FEMALE -> Constants.FEMALE
            Gender.MALE -> Constants.MALE
            Gender.UNKNOWN -> Constants.UNKNOWN
        }
        firebase.setUserProperty("gender", gender.string)
        FlurryAgent.setGender(g)
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
        facebook.logEvent(id, xpayload)
        firebase.logEvent(id, xpayload)
        FlurryAgent.logEvent(id, mpayload)
        DebugLogUtil.b("Analytics: $id${payload.joinToString(", ", " (", ")", transform = { "${it.first}:${it.second}" }).trim()}")
    }

    // ------------------------------------------
    fun persist(outState: Bundle) {
        outState.putString(SP_KEY_ANALYTICS_CACHE, toJson())
    }

    fun persist(spm: ISharedPrefsManager) {
        Timber.v("Saving analytics manager data... ${if (BuildConfig.DEBUG) toJson() else ""}")
        spm.saveByKey(SP_KEY_ANALYTICS_CACHE, toJson())
    }

    fun restore(savedInstanceState: Bundle?) {
        savedInstanceState
            ?.let { it.getString(SP_KEY_ANALYTICS_CACHE) }
            ?.let { restore(it) }
    }

    fun restore(spm: ISharedPrefsManager) {
        spm.getByKey(SP_KEY_ANALYTICS_CACHE)?.let { restore(it) }
    }

    private fun restore(it: String) {
        Timber.v("Restored analytics manager data: $it")
        try {
            val json = JSONObject(it)
            json.optJSONArray("consumedEventIds")?.let {
                val length = it.length()
                for (i in 0 until length) {
                    it.optString(i).takeIf { it.isNotBlank() }?.let { id -> consumedEventIds.add(id) }
                }
            }
            if (BuildConfig.DEBUG) {
                val xJson = toJson()
                Timber.v("Parsed restored analytics manager data: $xJson}")
                if (xJson != it) Timber.e("Parsing was incorrect!")
            }
        } catch (e: JSONException) {
            DebugLogUtil.e(e, "Failed to parse json: $it")
        }
    }

    private fun toJson(): String =
        "{\"consumedEventIds\":${consumedEventIds.joinToString(",", "[", "]", transform = { "\"$it\"" })}}"
}
