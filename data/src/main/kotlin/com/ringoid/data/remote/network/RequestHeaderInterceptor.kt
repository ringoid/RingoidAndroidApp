package com.ringoid.data.remote.network

import com.ringoid.data.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class RequestHeaderInterceptor : IRequestHeaderInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("x-ringoid-android-buildnum", "${BuildConfig.VERSION_CODE}")
            .addHeader("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}
