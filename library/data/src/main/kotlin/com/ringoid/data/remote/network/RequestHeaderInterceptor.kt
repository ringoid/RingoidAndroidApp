package com.ringoid.data.remote.network

import com.ringoid.debug.DebugLogUtil
import com.ringoid.report.log.Report
import okhttp3.Interceptor
import okhttp3.Response

class RequestHeaderInterceptor(private val appVersion: Int) : IRequestHeaderInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("x-ringoid-android-buildnum", "$appVersion")
            .addHeader("Content-Type", "application/json")
//            .addHeader("Accept-Encoding", "identity")  // always present, can be omitted
//            .addHeader("Connection","close")  // 'keep-alive' is default for HTTP/1.1 so change this to 'close'
            .build()

        val requestUrl = request.url.toString()

        DebugLogUtil.d("Request: $requestUrl")
        Report.breadcrumb("Request", "url" to requestUrl)

        return chain.proceed(request)
    }
}
