package com.ringoid.data.remote.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.ringoid.data.BuildConfig
import com.ringoid.data.remote.network.IRequestHeaderInterceptor
import com.ringoid.data.remote.network.RequestHeaderInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class CloudModule {

    @Provides @Singleton
    fun provideGson(): Gson =
        GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()

    @Provides @Singleton
    fun provideHttpLoggingInterceptor(): LoggingInterceptor {
        return LoggingInterceptor.Builder()
            .loggable(BuildConfig.DEBUG)
            .setLevel(Level.BASIC)
            .log(Platform.INFO)
            .request("Request")
            .response("Response")
//            .addHeader("version", BuildConfig.VERSION_NAME)
            .build()
    }

    @Provides @Singleton
    fun provideRequestHeaderInterceptor(): IRequestHeaderInterceptor =
        RequestHeaderInterceptor()

    @Provides @Singleton
    fun provideOkHttpClient(requestInterceptor: IRequestHeaderInterceptor,
                            logInterceptor: LoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(requestInterceptor)
            .addInterceptor(logInterceptor)
            .readTimeout(16, TimeUnit.SECONDS)
            .connectTimeout(16, TimeUnit.SECONDS)
            .build()

    @Provides @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit.Builder =
            Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
}
