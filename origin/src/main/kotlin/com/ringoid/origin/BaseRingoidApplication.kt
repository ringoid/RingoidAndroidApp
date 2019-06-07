package com.ringoid.origin

import android.content.res.Configuration
import android.os.StrictMode
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.flurry.android.FlurryAgent
import com.ringoid.base.IBaseRingoidApplication
import com.ringoid.base.IImagePreviewReceiver
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.push.UpdatePushTokenUseCase
import com.ringoid.domain.manager.IUserSettingsManager
import com.ringoid.domain.memory.ILoginInMemoryCache
import com.ringoid.domain.scope.UserScopeProvider
import com.ringoid.utility.manager.LocaleManager
import dagger.android.support.DaggerApplication
import io.branch.referral.Branch
import io.fabric.sdk.android.Fabric
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import io.sentry.Sentry
import leakcanary.LeakSentry
import timber.log.Timber
import java.io.IOException
import java.net.SocketException
import java.util.*
import javax.inject.Inject

abstract class BaseRingoidApplication : DaggerApplication(), IBaseRingoidApplication {

    override val calendar: Calendar = Calendar.getInstance()
    @Inject override lateinit var localeManager: LocaleManager
    @Inject override lateinit var imagePreviewReceiver: IImagePreviewReceiver
    @Inject override lateinit var loginInMemoryCache: ILoginInMemoryCache
    @Inject override lateinit var userScopeProvider: UserScopeProvider
    @Inject override lateinit var userSettingsManager: IUserSettingsManager
    @Inject override lateinit var updatePushTokenUseCase: UpdatePushTokenUseCase

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate() {
        super.onCreate()
        Timber.d("Starting ${javaClass.simpleName}")
        initializeResources()
        initializeLeakDetection()
        initializeCrashlytics()
        initializeLogger()  // Logger must be initialized to show logs at the very beginning
        initializeProgrammingTools()
        initializeRxErrorHandler()

        imagePreviewReceiver.register()  // app-wide broadcast receiver doesn't need to unregister
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        localeManager.setLocale(this)
    }

    /* Leak detection */
    // ------------------------------------------------------------------------
    private fun initializeLeakDetection() {
        // suitable for production
        LeakSentry.config = LeakSentry.config.copy(watchFragmentViews = false)

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build())

            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build())
        }
    }

    /* Logger */
    // ------------------------------------------------------------------------
    private fun initializeLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String =
                    packageName + ":" + super.createStackElementTag(element) + ":" + element.lineNumber
            })
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }

    /* Programming Tools */
    // --------------------------------------------------------------------------------------------
    private fun initializeProgrammingTools() {
        Branch.getAutoInstance(this)
        FlurryAgent.Builder()
            .withLogEnabled(true)
            .withCaptureUncaughtExceptions(true)
            .withContinueSessionMillis(10000)
            .withLogLevel(Log.VERBOSE)
            .build(this, com.ringoid.domain.BuildConfig.FLURRY_API_KEY)
        Sentry.init(com.ringoid.domain.BuildConfig.SENTRY_DSN)
    }

    /* Resources */
    // ------------------------------------------------------------------------
    private fun initializeResources() {
        localeManager.setLocale(this)
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
            DebugLogUtil.e(e)
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
