package com.ringoid.data.remote.network

import com.ringoid.report.log.Report
import dagger.Reusable
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * @see https://github.com/facebook/fresco/issues/1180
 */
@Reusable
class ImageHttpRequestInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        when (response.code) {
            HttpURLConnection.HTTP_NOT_FOUND ->
                Report.e("Image not found (http error 404)",
                         extras = listOf("imageUrl" to request.url.toString()))
        }
        return response
    }
}
