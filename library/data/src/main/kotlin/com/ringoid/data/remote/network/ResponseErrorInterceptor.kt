package com.ringoid.data.remote.network

import com.ringoid.datainterface.remote.model.BaseResponse
import com.ringoid.debug.DebugLogUtil
import com.ringoid.report.exception.NetworkUnexpected
import com.ringoid.report.log.SentryUtil
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class ResponseErrorInterceptor : IResponseErrorInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        fun logError(e: Throwable, errorMessage: String) {
            Timber.e(e, errorMessage)
            DebugLogUtil.e(e, errorMessage)
            SentryUtil.capture(e, errorMessage)
        }

        // --------------------------------------
        DebugLogUtil.d2("Response: chain prepare START")

        val request = chain.request()
        val requestUrl = request.url
        val errorMessage: String
        val unexpected: String?

        DebugLogUtil.d2("Response: chain prepare END")

        try {
            val response = chain.proceed(request)
            DebugLogUtil.d2("Response: $requestUrl: success=${response.isSuccessful}")
            if (!response.isSuccessful) {  // code not 200-300
                Timber.w("Unsuccessful network response, code: ${response.code}")
            }
            return response
        } catch (e: SocketTimeoutException) {
            errorMessage = "Connection timed out"
            unexpected = NetworkUnexpected.ERROR_CONNECTION_TIMED_OUT
            logError(e, errorMessage)
        } catch (e: SSLHandshakeException) {
            errorMessage = "Connection is not secure"
            unexpected = NetworkUnexpected.ERROR_CONNECTION_INSECURE
            logError(e, errorMessage)
        } catch (e: UnknownHostException) {
            errorMessage = "No network connection"
            unexpected = NetworkUnexpected.ERROR_NO_CONNECTION
            logError(e, errorMessage)
        } catch (e: Throwable) {
            errorMessage = "Other error"
            unexpected = null
            logError(e, errorMessage)
        }

        val body = BaseResponse(requestUrl = request.url.toString(), unexpected = unexpected)
        return Response.Builder()
            .code(200)
            .message(errorMessage)
            .protocol(Protocol.HTTP_1_1)
            .request(request)
            .body(body.toJson().toResponseBody("application/json".toMediaTypeOrNull()))
            .build()
    }
}
