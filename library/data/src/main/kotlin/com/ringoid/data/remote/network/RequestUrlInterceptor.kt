package com.ringoid.data.remote.network

import com.ringoid.debug.DebugLogUtil
import com.ringoid.report.log.SentryUtil
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RequestUrlInterceptor : IRequestUrlInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestUrl = request.url.toString()
        DebugLogUtil.d("Request: $requestUrl")
        SentryUtil.breadcrumb("Request", "url" to requestUrl)
        try {
            return chain.proceed(request)
        } catch (e: Throwable) {
            DebugLogUtil.d("Request: chain failed [$requestUrl]: ${e.message}")
            SentryUtil.capture(e, "Chain proceed has failed", extras = listOf("url" to requestUrl, "cause" to (e.message ?: "")))
            throw IOException("Chain proceed has failed", e)
        }
    }
}
