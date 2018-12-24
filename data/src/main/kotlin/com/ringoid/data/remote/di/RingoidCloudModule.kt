package com.ringoid.data.remote.di

import com.orcchg.githubuser.domain.BuildConfig
import com.ringoid.data.remote.RingoidRestAdapter
import com.ringoid.data.remote.network.ApiUrlBase
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
            ApiUrlBase.PRODUCTION -> "https://prod.ringoidapp.com"
            ApiUrlBase.STAGING -> "https://stage.ringoidapp.com"
        }
        return retrofit.baseUrl(apiUrl).build().create(RingoidRestAdapter::class.java)
    }
}
