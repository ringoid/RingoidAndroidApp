package com.ringoid.data.remote.di

import com.ringoid.data.remote.network.ImageHttpRequestInterceptor
import com.ringoid.domain.di.ImageLoad
import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Named

@Module
class ImageCloudModule {

    @Provides @Reusable @Named("ImageResponseErrors")
    fun provideImageResponseErrorInterceptor(interceptor: ImageHttpRequestInterceptor)
            : Interceptor = interceptor

    /**
     * @see https://github.com/facebook/fresco/issues/385
     */
    @Provides @ImageLoad
    fun provideOkHttpClient(
            @Named("ImageResponseErrors") responseInterceptor: Interceptor)
            : OkHttpClient =
        OkHttpClient.Builder()
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
//            .addInterceptor(responseInterceptor)
            .readTimeout(16, TimeUnit.SECONDS)
            .connectTimeout(16, TimeUnit.SECONDS)
            .writeTimeout(16, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
}
