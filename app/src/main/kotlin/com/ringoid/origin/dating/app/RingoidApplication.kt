package com.ringoid.origin.dating.app

import com.ringoid.data.remote.di.CloudModule
import com.ringoid.data.remote.di.RingoidCloudModule
import com.ringoid.data.remote.di.SystemCloudModule
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.origin.BaseRingoidApplication
import com.ringoid.origin.dating.app.di.ApplicationComponent
import com.ringoid.origin.dating.app.di.DaggerApplicationComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class RingoidApplication : BaseRingoidApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerApplicationComponent.builder()
            .application(this)
            .applicationContext(applicationContext)
            .cloudModule(CloudModule(appVersion = BuildConfig.BUILD_NUMBER))
            .ringoidCloudModule(RingoidCloudModule())
            .systemCloudModule(SystemCloudModule())
            .create(this)
            .also { DebugLogUtil.connectToDb((it as ApplicationComponent).debugLogDao()) }
}
