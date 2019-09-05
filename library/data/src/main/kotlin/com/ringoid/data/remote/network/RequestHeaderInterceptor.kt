package com.ringoid.data.remote.network

import com.ringoid.debug.DebugLogUtil
import com.ringoid.report.log.SentryUtil
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RequestHeaderInterceptor(private val appVersion: Int) : IRequestHeaderInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("x-ringoid-android-buildnum", "$appVersion")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept-Encoding", "identity")  // always present, can be omitted
            .addHeader("Connection","close")  // 'keep-alive' is default for HTTP/1.1 so change this to 'close'
            .build()

        val requestUrl = request.url.toString()

        DebugLogUtil.d("Request: $requestUrl")
        SentryUtil.breadcrumb("Request", "url" to requestUrl)

        try {
            return chain.proceed(request)
        } catch (e: Throwable) {
            DebugLogUtil.d("Request: chain failed [$requestUrl]: ${e.message}")
            SentryUtil.capture(e, "Chain proceed has failed",
                               extras = listOf("url" to requestUrl, "cause" to (e.message ?: ""),
                                               "from" to javaClass.simpleName))
            throw IOException("Chain proceed has failed", e)
        }
    }
}
