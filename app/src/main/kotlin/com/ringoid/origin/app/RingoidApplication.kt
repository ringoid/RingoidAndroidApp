package com.ringoid.origin.app

import com.ringoid.data.remote.di.CloudModule
import com.ringoid.data.remote.di.RingoidCloudModule
import com.ringoid.data.remote.network.ApiUrlBase
import com.ringoid.domain.BuildConfig
import com.ringoid.origin.BaseRingoidApplication
import com.ringoid.origin.app.di.DaggerApplicationComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class RingoidApplication : BaseRingoidApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerApplicationComponent.builder()
            .application(this)
            .applicationContext(applicationContext)
            .cloudModule(CloudModule(appVersion = BuildConfig.BUILD_NUMBER))
            .ringoidCloudModule(RingoidCloudModule(apiUrlBase = ApiUrlBase.DEVELOP))
            .create(this)
}
