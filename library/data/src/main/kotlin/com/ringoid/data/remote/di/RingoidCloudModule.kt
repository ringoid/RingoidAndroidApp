package com.ringoid.data.remote.di

import com.ringoid.data.remote.api.RingoidRestAdapter
import com.ringoid.data.remote.debug.CloudDebug
import com.ringoid.data.remote.network.ApiUrlBase
import com.ringoid.domain.BuildConfig
import com.ringoid.utility.DebugOnly
import com.ringoid.domain.debug.ICloudDebug
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module(includes = [CloudModule::class])
class RingoidCloudModule(private val apiUrlBase: ApiUrlBase = ApiUrlBase.DEFAULT,
                         private val customApiUrl: String = BuildConfig.API_URL) {

    @Provides @Singleton
    fun provideRestAdapter(retrofit: Retrofit.Builder): RingoidRestAdapter {
        val apiUrl = when (apiUrlBase) {
            ApiUrlBase.CUSTOM -> customApiUrl
            ApiUrlBase.DEFAULT -> BuildConfig.API_URL
            ApiUrlBase.DEVELOP -> "https://test.ringoidapp.com"
            ApiUrlBase.PRODUCTION -> "https://prod.ringoidapp.com"
            ApiUrlBase.STAGING -> "https://stage.ringoidapp.com"
        }
        return retrofit.baseUrl(apiUrl).build().create(RingoidRestAdapter::class.java)
    }

    @Provides @Singleton @DebugOnly
    fun provideCloudDebug(cloudDebug: CloudDebug): ICloudDebug = cloudDebug
}
