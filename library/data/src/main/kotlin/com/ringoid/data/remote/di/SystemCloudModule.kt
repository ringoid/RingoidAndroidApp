package com.ringoid.data.remote.di

import com.ringoid.data.remote.api.SlackRestAdapter
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module(includes = [CloudModule::class])
class SystemCloudModule {

    @Provides @Singleton
    fun provideSlackRestAdapter(retrofit: Retrofit.Builder): SlackRestAdapter =
        retrofit.baseUrl(SlackRestAdapter.SLACK_URL).build().create(SlackRestAdapter::class.java)
}
