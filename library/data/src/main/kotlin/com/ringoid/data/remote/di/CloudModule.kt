package com.ringoid.data.remote.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.ringoid.data.BuildConfig
import com.ringoid.data.remote.network.IRequestHeaderInterceptor
import com.ringoid.data.remote.network.IResponseErrorInterceptor
import com.ringoid.data.remote.network.RequestHeaderInterceptor
import com.ringoid.data.remote.network.ResponseErrorInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class CloudModule(private val appVersion: Int) {

    @Provides @Singleton
    fun provideGson(): Gson =
        GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()

    @Provides @Singleton
    fun provideHttpLoggingInterceptor(): LoggingInterceptor =
        LoggingInterceptor.Builder()
            .loggable(BuildConfig.DEBUG)
            .setLevel(Level.BASIC)
            .log(Platform.INFO)
            .request("Request")
            .response("Response")
            .build()

    @Provides @Singleton
    fun provideRequestHeaderInterceptor(): IRequestHeaderInterceptor =
        RequestHeaderInterceptor(appVersion = appVersion)

    @Provides @Singleton
    fun provideResponseErrorInterceptor(): IResponseErrorInterceptor = ResponseErrorInterceptor()

    @Provides @Singleton
    fun provideOkHttpClient(requestInterceptor: IRequestHeaderInterceptor,
                            responseInterceptor: IResponseErrorInterceptor,
                            logInterceptor: LoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
            .addInterceptor(requestInterceptor)
            .addInterceptor(responseInterceptor)
            .let { if (BuildConfig.DEBUG) it.addInterceptor(logInterceptor) else it }
            .readTimeout(12, TimeUnit.SECONDS)
            .connectTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()

    @Provides @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit.Builder =
            Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
}
