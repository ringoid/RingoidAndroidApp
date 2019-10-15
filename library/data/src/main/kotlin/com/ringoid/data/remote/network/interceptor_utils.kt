package com.ringoid.data.remote.network

import com.ringoid.debug.DebugLogUtil
import com.ringoid.report.log.Report
import java.io.IOException

fun Throwable.isCanceledNetworkRequest(): Boolean =
    this is IOException && message?.contains("Canceled") == true

fun Throwable.reportNetworkInterceptionError(requestUrl: String, from: String) {
    DebugLogUtil.e(this, "Chain failed [$requestUrl]: $message")
    if (isCanceledNetworkRequest() && requestUrl.contains("/feeds/chat")) {
        return  // don't report canceled /feeds/chat requests
    }
    Report.capture(this, "Chain proceed has failed",
        extras = listOf("url" to requestUrl, "cause" to (message ?: ""),
                        "inner cause" to "${cause?.javaClass?.simpleName}",
                        "inner cause msg" to "${cause?.message}",
                        "from" to from /** normally calling object's class */))
}

@Throws(IOException::class)
fun Throwable.reportNetworkInterceptionErrorAndThrow(requestUrl: String, from: String): Throwable {
    reportNetworkInterceptionError(requestUrl, from)
    return IOException("Chain proceed has failed", this)
}
