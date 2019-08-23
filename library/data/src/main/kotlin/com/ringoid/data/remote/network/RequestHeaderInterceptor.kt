package com.ringoid.data.remote.network

import okhttp3.Interceptor
import okhttp3.Response

class RequestHeaderInterceptor(private val appVersion: Int) : IRequestHeaderInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("x-ringoid-android-buildnum", "$appVersion")
            .addHeader("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}
