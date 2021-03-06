package com.ringoid.origin

import android.content.res.Configuration
import android.content.res.Resources
import android.os.StrictMode
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.ringoid.base.IBaseRingoidApplication
import com.ringoid.base.IImagePreviewReceiver
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.interactor.push.UpdatePushTokenUseCase
import com.ringoid.domain.manager.IUserSettingsManager
import com.ringoid.domain.memory.ILoginInMemoryCache
import com.ringoid.domain.scope.UserScopeProvider
import com.ringoid.utility.manager.LocaleManager
import dagger.android.support.DaggerApplication
import io.fabric.sdk.android.Fabric
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
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

    override fun getRes(): Resources = resources

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate() {
        initializeCrashlytics()  // Crashlytics must be initialized before logging service
        initializeLogger()  // Logger must be initialized to show logs at the very beginning
        super.onCreate()
        Timber.d("Starting ${javaClass.simpleName}")
        initializeResources()
        initializeRxErrorHandler()
//        initializeStrictMode()  // ignore StrictMode alerts for SDK's initializations
        imagePreviewReceiver.register()  // app-wide broadcast receiver doesn't need to unregister
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        LocaleManager.initLocale(applicationContext)  // refresh local on configuration change
    }

    /* Debugging */
    // ------------------------------------------------------------------------
    private fun initializeStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
//                    .penaltyDeath()
                    .build())

            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
//                    .penaltyDeath()
                    .build())
        }
    }

    /* Logger */
    // ------------------------------------------------------------------------
    private fun initializeLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String =
                    "$packageName:${super.createStackElementTag(element)}:${element.lineNumber}"//-${Thread.currentThread().name}"
            })
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }

    /* Resources */
    // ------------------------------------------------------------------------
    private fun initializeResources() {
        LocaleManager.initLocale(applicationContext)  // initialize locale in app
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
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
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
