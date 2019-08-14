package com.ringoid.origin.dating.app

import android.util.Log
import com.flurry.android.FlurryAgent
import com.ringoid.data.remote.di.CloudModule
import com.ringoid.data.remote.di.RingoidCloudModule
import com.ringoid.data.remote.di.SystemCloudModule
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.debug.BarrierLogUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.origin.BaseRingoidApplication
import com.ringoid.origin.dating.app.di.ApplicationComponent
import com.ringoid.origin.dating.app.di.DaggerApplicationComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.branch.referral.Branch
import io.sentry.Sentry
import leakcanary.LeakSentry

class RingoidApplication : BaseRingoidApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerApplicationComponent.builder()
            .application(this)
            .applicationContext(applicationContext)
            .cloudModule(CloudModule(appVersion = BuildConfig.BUILD_NUMBER))
            .ringoidCloudModule(RingoidCloudModule())
            .systemCloudModule(SystemCloudModule())
            .create(this)
            .also { BarrierLogUtil.connectToDb((it as ApplicationComponent).barrierLogDao()) }
            .also { DebugLogUtil.connectToDb((it as ApplicationComponent).debugLogDao()) }

    override fun onCreate() {
        super.onCreate()
        Branch.getAutoInstance(this)

        FlurryAgent.Builder()
            .withLogEnabled(true)
            .withCaptureUncaughtExceptions(true)
            .withContinueSessionMillis(10000)
            .withLogLevel(Log.VERBOSE)
            .build(this, BuildConfig.FLURRY_API_KEY)

        Sentry.init(BuildConfig.SENTRY_DSN)
        LeakSentry.config = LeakSentry.config.copy(watchFragmentViews = false)
    }
}
