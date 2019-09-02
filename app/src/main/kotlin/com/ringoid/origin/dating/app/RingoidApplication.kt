package com.ringoid.origin.dating.app

import android.os.HandlerThread
import android.util.Log
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
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
import com.ringoid.utility.image.ImageCacheTracker
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.branch.referral.Branch
import io.sentry.Sentry
import timber.log.Timber

class RingoidApplication : BaseRingoidApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerApplicationComponent.builder()
            .application(this)
            .applicationContext(applicationContext)
            .bgLooper(HandlerThread("BgLooper-1"))
            .cloudModule(CloudModule(appVersion = BuildConfig.BUILD_NUMBER))
            .ringoidCloudModule(RingoidCloudModule())
            .systemCloudModule(SystemCloudModule())
            .create(this)
            .also {
                (it as ApplicationComponent).bgLooper().let { bgLooper ->
                    if (!bgLooper.isAlive) {
                        Timber.v("Background looper has been prepared")
                        bgLooper.start()
                    }
                }
            }
            .also { BarrierLogUtil.connectToDb((it as ApplicationComponent).barrierLogDao()) }
            .also { DebugLogUtil.connectToDb((it as ApplicationComponent).debugLogDao()) }

    override fun onCreate() {
        super.onCreate()
        Branch.getAutoInstance(this)

        val frescoConfig =
            ImagePipelineConfig.newBuilder(this)
                .setImageCacheStatsTracker(ImageCacheTracker())
                .build()
        Fresco.initialize(this, frescoConfig)

        FlurryAgent.Builder()
            .withLogEnabled(true)
            .withCaptureUncaughtExceptions(true)
            .withContinueSessionMillis(10000)
            .withLogLevel(Log.VERBOSE)
            .build(this, BuildConfig.FLURRY_API_KEY)

        Sentry.init(BuildConfig.SENTRY_DSN)
    }
}
