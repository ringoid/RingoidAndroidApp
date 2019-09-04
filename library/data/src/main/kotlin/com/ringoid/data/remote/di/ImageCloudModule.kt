package com.ringoid.data.remote.di

import com.ringoid.domain.di.ImageLoader
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.util.*
import java.util.concurrent.TimeUnit

@Module
class ImageCloudModule {

    /**
     * @see https://github.com/facebook/fresco/issues/385
     */
    @Provides @ImageLoader
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
            .readTimeout(16, TimeUnit.SECONDS)
            .connectTimeout(16, TimeUnit.SECONDS)
            .writeTimeout(16, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
}
