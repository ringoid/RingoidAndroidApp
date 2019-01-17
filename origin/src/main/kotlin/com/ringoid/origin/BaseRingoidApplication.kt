package com.ringoid.origin

import android.content.Context
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import dagger.android.support.DaggerApplication
import io.fabric.sdk.android.Fabric
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import java.io.IOException
import java.net.SocketException
import java.util.*

abstract class BaseRingoidApplication : DaggerApplication() {

    private var refWatcher: RefWatcher? = null

    val calendar = Calendar.getInstance()

    companion object {
        fun refWatcher(context: Context?): RefWatcher? =
            (context?.applicationContext as? BaseRingoidApplication)?.refWatcher
    }

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This processSingle is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this processSingle.
            return
        }
        Timber.d("Starting ${javaClass.simpleName}")
        initializeResources()
//        initializeLeakDetection()
        initializeCrashlytics()
        initializeLogger()  // Logger must be initialized to show logs at the very beginning
        initializeRxErrorHandler()
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

    /* Resources */
    // ------------------------------------------------------------------------
    private fun initializeResources() {
        AppRes.init(applicationContext)
    }

    /* Rx */
    // ------------------------------------------------------------------------
    private fun initializeRxErrorHandler() {
        RxJavaPlugins.setErrorHandler {
            var e = it
            when (it) {
                is UndeliverableException -> e = it.cause
                is IOException, is SocketException -> {
                    Timber.w(it, "Fine, irrelevant network problem or API that throws on cancellation")
                    return@setErrorHandler
                }
                is InterruptedException -> {
                    Timber.w(it, "Fine, some blocking code was interrupted by a dispose call")
                    return@setErrorHandler
                }
                is NullPointerException, is IllegalArgumentException -> {
                    Timber.e(it, "That's likely a bug in the application")
                    Thread.currentThread().uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), e)
                    return@setErrorHandler
                }
                is IllegalStateException -> {
                    Timber.w(it, "That's a bug in RxJava or in a custom operator")
                    Thread.currentThread().uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), e)
                    return@setErrorHandler
                }
            }
            Timber.e(e, "Undeliverable exception received, not sure what to do")
        }
    }

    /* Crashlytics */
    // --------------------------------------------------------------------------------------------
    private fun initializeCrashlytics() {
        val core = CrashlyticsCore.Builder()
            .disabled(BuildConfig.DEBUG)
            .build()
        Fabric.with(this, Crashlytics.Builder().core(core).build())
    }

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
