package com.ringoid.origin.dating.app

import android.os.HandlerThread
import android.util.Log
import com.facebook.cache.disk.DiskCacheConfig
import com.facebook.common.util.ByteConstants
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory
import com.flurry.android.FlurryAgent
import com.ringoid.data.remote.di.CloudModule
import com.ringoid.data.remote.di.DaggerImageCloudComponent
import com.ringoid.data.remote.di.RingoidCloudModule
import com.ringoid.data.remote.di.SystemCloudModule
import com.ringoid.debug.DebugLogUtil
import com.ringoid.debug.barrier.BarrierLogUtil
import com.ringoid.domain.BuildConfig
import com.ringoid.origin.BaseRingoidApplication
import com.ringoid.origin.dating.app.di.ApplicationComponent
import com.ringoid.origin.dating.app.di.DaggerApplicationComponent
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
        initializeAnalytics()
        initializeImageLoader()
    }

    // --------------------------------------------------------------------------------------------
    private fun initializeAnalytics() {
        Branch.getAutoInstance(this)

        FlurryAgent.Builder()
            .withLogEnabled(true)
            .withCaptureUncaughtExceptions(true)
            .withContinueSessionMillis(10000)
            .withLogLevel(Log.VERBOSE)
            .build(this, BuildConfig.FLURRY_API_KEY)

        Sentry.init(BuildConfig.SENTRY_DSN)
    }

    private fun initializeImageLoader() {
        val diskCacheConfig = DiskCacheConfig.newBuilder(this)
            .setMaxCacheSize(100L * ByteConstants.MB)
            .setMaxCacheSizeOnLowDiskSpace(25L * ByteConstants.MB)
            .setMaxCacheSizeOnVeryLowDiskSpace(5L * ByteConstants.MB)
            .build()

        val networkClient = DaggerImageCloudComponent.create()
            .networkClientForImageLoader()

        val frescoConfig =
            OkHttpImagePipelineConfigFactory.newBuilder(this, networkClient)
//                .setImageCacheStatsTracker(ImageCacheTracker())
//                .setMainDiskCacheConfig(diskCacheConfig)
                .experiment()
                .setBitmapPrepareToDraw(true, 0, Integer.MAX_VALUE, true)
                .experiment()
                .setDecodeCancellationEnabled(true)
                .build()
        Fresco.initialize(this, frescoConfig)
    }
}
