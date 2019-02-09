package com.ringoid.data.remote.network

import com.ringoid.domain.SentryUtil
import okhttp3.*
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class ResponseErrorInterceptor : IResponseErrorInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val errorCode: Int ; val errorCodeId: String ; val errorMessage: String
        try {
            val response = chain.proceed(request)
            if (!response.isSuccessful) {  // code not 200-300
                Timber.v("Sending event through bus: network error")
                EventBus.getDefault().post(NetworkError(response.code(), request = request))
            }
            return response
        } catch (e: SocketTimeoutException) {
            errorCode = 1; errorCodeId = NetworkManager.CONNECTION_TIMEOUT_ERROR_CODE; errorMessage = "Connection timed out"
            Timber.e(e, "Connection timed out")
            SentryUtil.capture(e, "Connection timed out")
            EventBus.getDefault().post(ConnectionTimeout(e.message))
        } catch (e: SSLHandshakeException) {
            errorCode = 2; errorCodeId = NetworkManager.CONNECTION_INSECURE_ERROR_CODE; errorMessage = "Connection is not secure"
            Timber.e(e, "SSL Handshake has failed")
            SentryUtil.capture(e, "SSL Handshake has failed")
            EventBus.getDefault().post(SSLError(e.message))
        } catch (e: UnknownHostException) {
            errorCode = 0; errorCodeId = NetworkManager.CONNECTION_FAIL_ERROR_CODE; errorMessage = "No network connection"
            Timber.v("Sending event through bus: connection error")
            EventBus.getDefault().post(NetworkError(request = request))
        }
        return Response.Builder()
            .code(errorCode)
            .message(errorMessage)
            .protocol(Protocol.HTTP_1_1)
            .request(request)
            .body(ResponseBody.create(MediaType.parse("application/json"), "{\"code\":\"$errorCodeId\",\"message\":\"$errorMessage\"}"))
            .build()  // empty response
    }
}
