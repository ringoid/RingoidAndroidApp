package com.ringoid.data.remote.network

import com.ringoid.domain.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class RequestHeaderInterceptor : IRequestHeaderInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("x-ringoid-android-buildnum", "${BuildConfig.MINOR_VERSION}")
            .addHeader("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}
