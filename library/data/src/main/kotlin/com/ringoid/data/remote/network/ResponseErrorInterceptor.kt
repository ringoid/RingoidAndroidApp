package com.ringoid.data.remote.network

import com.ringoid.datainterface.remote.model.BaseResponse
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.debug.DebugNetworkUtil
import com.ringoid.domain.log.SentryUtil
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

    companion object {
        const val ERROR_CONNECTION_INSECURE = "ErrorConnectionInsecure"
        const val ERROR_CONNECTION_TIMED_OUT = "ErrorConnectionTimedOut"
        const val ERROR_NO_CONNECTION = "ErrorNoConnection"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val unexpected: String? ; val errorMessage: String
        try {
            if (BuildConfig.DEBUG && DebugNetworkUtil.networkException != null) {
                DebugLogUtil.w("Simulating network error...")
                throw DebugNetworkUtil.networkException!!
            }
            val response = chain.proceed(request)
            if (!response.isSuccessful) {  // code not 200-300
                Timber.w("Unsuccessful network response, code: ${response.code}")
            }
            return response
        } catch (e: SocketTimeoutException) {
            errorMessage = "Connection timed out" ; Timber.e(e)
            unexpected = ERROR_CONNECTION_TIMED_OUT
            DebugLogUtil.e(e)
        } catch (e: SSLHandshakeException) {
            errorMessage = "Connection is not secure" ; Timber.e(e)
            SentryUtil.capture(e, errorMessage)
            unexpected = ERROR_CONNECTION_INSECURE
            DebugLogUtil.e(e)
        } catch (e: UnknownHostException) {
            errorMessage = "No network connection" ; Timber.e(e)
            unexpected = ERROR_NO_CONNECTION
            DebugLogUtil.e(e)
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
