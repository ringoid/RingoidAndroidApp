package com.ringoid.origin

import android.app.Application
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.orcchg.githubuser.origin.BuildConfig
import timber.log.Timber

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeLogger()  // Logger must be initialized to show logs at the very beginning
    }

    /* Logger */
    // ------------------------------------------------------------------------
    private fun initializeLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String {
                    return packageName + ":" + super.createStackElementTag(element) + ":" + element.lineNumber
                }
            })
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }

    /* Crashlytics */
    // --------------------------------------------------------------------------------------------
    /**
     * {@see https://blog.xmartlabs.com/2015/07/09/Android-logging-with-Crashlytics-and-Timber/}
     * Comment: [Timber.Tree] only supplies the tag when it was explicitly set.
     * In most cases, tag will be null. If you want the tag to be extracted from the log,
     * you need to extend [Timber.DebugTree] instead.
     */
    inner class CrashlyticsTree : Timber.DebugTree() {

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE) {
                return
            }

            Crashlytics.setInt("priority", priority)
            Crashlytics.setString("tag", tag)
            Crashlytics.setString("message", message)

            if (t == null) {
                Crashlytics.log(priority, tag, message)
            } else {
                Crashlytics.logException(t)
            }
        }
    }
}
