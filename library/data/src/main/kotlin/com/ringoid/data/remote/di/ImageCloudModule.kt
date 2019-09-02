package com.ringoid.data.remote.di

import com.ringoid.domain.di.ImageLoader
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@Module
class ImageCloudModule {

    /**
     * @see https://github.com/facebook/fresco/issues/385
     */
    @Provides @ImageLoader
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .readTimeout(16, TimeUnit.SECONDS)
            .connectTimeout(16, TimeUnit.SECONDS)
            .writeTimeout(16, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
}
