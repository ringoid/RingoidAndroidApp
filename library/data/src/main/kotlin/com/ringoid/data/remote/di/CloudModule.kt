package com.ringoid.data.remote.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.ringoid.data.BuildConfig
import com.ringoid.data.remote.network.RequestHeaderInterceptor
import com.ringoid.data.remote.network.ResponseErrorInterceptor
import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class CloudModule(private val appVersion: Int) {

    @Provides @Singleton
    fun provideGson(): Gson =
        GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()

    @Provides @Reusable @Named("Logging")
    fun provideHttpLoggingInterceptor(): Interceptor =
        LoggingInterceptor.Builder()
            .loggable(BuildConfig.DEBUG)
            .setLevel(Level.BASIC)
            .log(Platform.INFO)
            .request("Request")
            .response("Response")
            .enableAndroidStudio_v3_LogsHack(true)
            .build()

    @Provides @Reusable @Named("RequestHeaders")
    fun provideRequestHeaderInterceptor(): Interceptor =
        RequestHeaderInterceptor(appVersion = appVersion)

    @Provides @Reusable @Named("ResponseErrors")
    fun provideResponseErrorInterceptor(interceptor: ResponseErrorInterceptor): Interceptor = interceptor

    @Provides @Singleton
    fun provideOkHttpClient(
            @Named("RequestHeaders") requestInterceptor: Interceptor,
            @Named("ResponseErrors") responseInterceptor: Interceptor,
            @Named("Logging") logInterceptor: Interceptor)
            : OkHttpClient =
        OkHttpClient.Builder()
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
            .addInterceptor(requestInterceptor)
            .addInterceptor(responseInterceptor)
            .let { if (BuildConfig.DEBUG) it.addInterceptor(logInterceptor) else it }
            .readTimeout(12, TimeUnit.SECONDS)
            .connectTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

    @Provides @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit.Builder =
            Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
}
