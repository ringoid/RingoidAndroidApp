package com.ringoid.repository.test

import com.ringoid.data.handleErrorNoTrace
import com.ringoid.datainterface.remote.model.BaseResponse
import com.ringoid.utility.SysTimber
import com.ringoid.utility.stackTraceString
import io.reactivex.Single
import org.junit.Assert
import org.junit.Test

class HandleErrorTest {

    @Test
    fun testDebugHandleErrorDownstream() {
        var success = false

        Single.just(BaseResponse() /** successful response */)
            .doOnSubscribe { SysTimber.i("Downstream subscribe on debug") }
            .doOnSuccess { SysTimber.v("Upstream debug response: $it") }
            .doOnError { SysTimber.v("Upstream failed al debug retries: $it") }
            .flatMap {
                Single.just(BaseResponse(errorCode = "DownstreamDebugError", errorMessage = "Downstream Debug error"))
                    .doOnSubscribe { SysTimber.v("Internal subscribe debug") }
                    .doOnSuccess { SysTimber.v("Internal debug response: $it") }
                    .doOnError { SysTimber.v("Internal failed all retries: $it") }
                    .map { SysTimber.v("Internal upstream debug mapping 1"); it }
            }
            .map { SysTimber.v("Upstream debug mapping 1"); it }
            .handleErrorNoTrace(count = 4)
            .map { SysTimber.d("Downstream debug mapping 1"); it }
            .doOnSuccess { SysTimber.d("Downstream debug response: $it") }
            .doOnError { SysTimber.d("Downstream failed all debug retries: $it") }
            .ignoreElement()
            .subscribe({
                SysTimber.d("Downstream debug finish")
                success = true
            }, SysTimber::e)

        Assert.assertTrue(success)
    }

    @Test
    fun testDebugHandleErrorMultistream() {
        var success = false

        Single.just(BaseResponse() /** successful response */)
            .doOnSubscribe { SysTimber.i("Multistream subscribe on debug") }
            .doOnSuccess { SysTimber.v("Upstream debug response: $it") }
            .doOnError { SysTimber.v("Upstream failed al debug retries: $it") }
            .map { SysTimber.v("Upstream debug mapping 0"); it }
            .handleErrorNoTrace(count = 4)
            .map { SysTimber.d("Downstream debug mapping 0"); it }
            .flatMap {
                Single.just(BaseResponse(errorCode = "DownstreamDebugError", errorMessage = "Downstream Debug error"))
                    .doOnSubscribe { SysTimber.v("Internal upstream subscribe debug") }
                    .doOnSuccess { SysTimber.v("Internal upstream debug response: $it") }
                    .doOnError { SysTimber.v("Internal upstream failed all retries: $it") }
                    .map { SysTimber.v("Internal upstream debug mapping 0"); it }
                    .handleErrorNoTrace(count = 4)
                    .map { SysTimber.d("Internal downstream debug mapping 0"); it }
            }
            .map { SysTimber.d("Downstream debug mapping 1"); it }
            .doOnSuccess { SysTimber.d("Downstream debug response: $it") }
            .doOnError { SysTimber.d("Downstream failed all debug retries: $it") }
            .ignoreElement()
            .subscribe({
                SysTimber.d("Multistream debug finish")
                success = true
            }, SysTimber::e)

        Assert.assertTrue(success)
    }

    @Test
    fun testDebugHandleErrorUpstream() {
        var success = false

        Single.just(BaseResponse(errorCode = "UpstreamDebugError", errorMessage = "Upstream Debug error"))
            .doOnSubscribe { SysTimber.i("Upstream subscribe on debug") }
            .doOnSuccess { SysTimber.v("Upstream debug response: $it") }
            .doOnError { SysTimber.v("Upstream failed all debug retries: $it") }
            .map { SysTimber.v("Upstream debug mapping 0"); it }
            .handleErrorNoTrace(count = 4)
            .map { SysTimber.d("Downstream debug mapping 0"); it }
            .doOnSuccess { SysTimber.d("Downstream debug response: $it") }
            .doOnError { SysTimber.d("Downstream failed all debug retries: ${it.stackTraceString()}") }
            .ignoreElement()
            .subscribe({
                SysTimber.d("Upstream debug finish")
                success = true
            }, SysTimber::e)

        Assert.assertTrue(success)
    }

    @Test
    fun testDebugHandleErrorStream() {
        var success = false

        Single.just(BaseResponse() /** successful response */)
            .doOnSubscribe { SysTimber.i("Stream subscribe on debug") }
            .doOnSuccess { SysTimber.v("Upstream debug response: $it") }
            .doOnError { SysTimber.v("Upstream failed al debug retries: $it") }
            .flatMap {
                Single.just(BaseResponse(errorCode = "DownstreamDebugError", errorMessage = "Downstream Debug error"))
                    .doOnSubscribe { SysTimber.v("Internal upstream subscribe debug") }
                    .doOnSuccess { SysTimber.v("Internal upstream debug response: $it") }
                    .doOnError { SysTimber.v("Internal upstream failed all retries: $it") }
                    .map { SysTimber.v("Internal upstream debug mapping 0"); it }
                    .handleErrorNoTrace(count = 4)
                    .map { SysTimber.d("Internal downstream debug mapping 0"); it }
            }
            .map { SysTimber.d("Downstream debug mapping 0"); it }
            .doOnSuccess { SysTimber.d("Downstream debug response: $it") }
            .doOnError { SysTimber.d("Downstream failed all debug retries: $it") }
            .ignoreElement()
            .subscribe({
                SysTimber.d("Stream debug finish")
                success = true
            }, SysTimber::e)

        Assert.assertTrue(success)
    }
}
