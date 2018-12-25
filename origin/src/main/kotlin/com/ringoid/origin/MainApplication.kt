package com.ringoid.origin

import android.app.Application
import android.content.Context
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.orcchg.githubuser.origin.BuildConfig
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import timber.log.Timber

class MainApplication : Application() {

    private var refWatcher: RefWatcher? = null

    companion object {
        fun getRefWatcher(context: Context?): RefWatcher? {
            val app = context?.applicationContext as? MainApplication
            return app?.refWatcher
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        initializeLeakDetection()
        initializeLogger()  // Logger must be initialized to show logs at the very beginning
    }

    /* Leak detection */
    // ------------------------------------------------------------------------
    private fun initializeLeakDetection() {
        if (BuildConfig.DEBUG) {
            refWatcher = LeakCanary.install(this)
        }
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
