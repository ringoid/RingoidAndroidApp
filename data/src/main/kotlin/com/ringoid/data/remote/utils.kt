package com.ringoid.data.remote

import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.SentryUtil
import com.ringoid.domain.model.IEssence
import io.reactivex.*
import okhttp3.MediaType
import okhttp3.RequestBody
import timber.log.Timber

fun IEssence.toBody(): RequestBody = RequestBody.create(MediaType.parse("application/json"), this.toJson())

const val DEFAULT_TAG = "response"

// ------------------------------------------------------------------------------------------------
fun Completable.logResponse(tag: String = ""): Completable = this  // no-op, for symmetry
inline fun <reified T : BaseResponse> Maybe<T>.logResponse(tag: String = ""): Maybe<T> = compose(logResponseMaybe<T>(tag))
inline fun <reified T : BaseResponse> Single<T>.logResponse(tag: String = ""): Single<T> = compose(logResponseSingle<T>(tag))
inline fun <reified T : BaseResponse> Flowable<T>.logResponse(tag: String = ""): Flowable<T> = compose(logResponseFlowable<T>(tag))
inline fun <reified T : BaseResponse> Observable<T>.logResponse(tag: String = ""): Observable<T> = compose(logResponseObservable<T>(tag))

inline fun <reified T : BaseResponse> logResponseMaybe(tag: String = ""): MaybeTransformer<T, T> =
    MaybeTransformer { it.doOnSuccess { logBaseResponse(it, tag) } }
inline fun <reified T : BaseResponse> logResponseSingle(tag: String = ""): SingleTransformer<T, T> =
    SingleTransformer { it.doOnSuccess { logBaseResponse(it, tag) } }
inline fun <reified T : BaseResponse> logResponseFlowable(tag: String = ""): FlowableTransformer<T, T> =
    FlowableTransformer { it.doOnNext { logBaseResponse(it, tag) } }
inline fun <reified T : BaseResponse> logResponseObservable(tag: String = ""): ObservableTransformer<T, T> =
    ObservableTransformer { it.doOnNext { logBaseResponse(it, tag) } }

inline fun <reified T : BaseResponse> logBaseResponse(it: T, tag: String = "") {
    SentryUtil.breadcrumb("Response [$tag]", "error code" to it.errorCode,
        "error message" to it.errorMessage, "repeat after" to "${it.repeatRequestAfter}",
        "request url" to "${it.requestUrl ?: ""}", "unexpected" to (it.unexpected ?: ""), "raw" to it.toString())
}

// ------------------------------------------------------------------------------------------------
fun Completable.checkResponseTime(tag: String = DEFAULT_TAG): Completable = compose(checkResponseTimeCompletable(tag))
inline fun <reified T> Maybe<T>.checkResponseTime(tag: String = DEFAULT_TAG): Maybe<T> = compose(checkResponseTimeMaybe(tag))
inline fun <reified T> Single<T>.checkResponseTime(tag: String = DEFAULT_TAG): Single<T> = compose(checkResponseTimeSingle(tag))
inline fun <reified T> Flowable<T>.checkResponseTime(tag: String = DEFAULT_TAG): Flowable<T> = compose(checkResponseTimeFlowable(tag))
inline fun <reified T> Observable<T>.checkResponseTime(tag: String = DEFAULT_TAG): Observable<T> = compose(checkResponseTimeObservable(tag))

fun checkResponseTimeCompletable(tag: String = DEFAULT_TAG): CompletableTransformer =
    CompletableTransformer {
        var startTime = 0L
        it
            .doOnSubscribe { startTime = System.currentTimeMillis() }
            .doFinally { checkElapsedTimeAndWarn(startTime, tag = tag) }
    }

inline fun <reified T> checkResponseTimeMaybe(tag: String = DEFAULT_TAG): MaybeTransformer<T, T> =
    MaybeTransformer {
        var startTime = 0L
        it
            .doOnSubscribe { startTime = System.currentTimeMillis() }
            .doFinally { checkElapsedTimeAndWarn(startTime, tag = tag) }
    }

inline fun <reified T> checkResponseTimeSingle(tag: String = DEFAULT_TAG): SingleTransformer<T, T> =
    SingleTransformer {
        var startTime = 0L
        it
            .doOnSubscribe { startTime = System.currentTimeMillis() }
            .doFinally { checkElapsedTimeAndWarn(startTime, tag = tag) }
    }

inline fun <reified T> checkResponseTimeFlowable(tag: String = DEFAULT_TAG): FlowableTransformer<T, T> =
    FlowableTransformer {
        var startTime = 0L
        it
            .doOnSubscribe { startTime = System.currentTimeMillis() }
            .doFinally { checkElapsedTimeAndWarn(startTime, tag = tag) }
    }

inline fun <reified T> checkResponseTimeObservable(tag: String = DEFAULT_TAG): ObservableTransformer<T, T> =
    ObservableTransformer {
        var startTime = 0L
        it
            .doOnSubscribe { startTime = System.currentTimeMillis() }
            .doFinally { checkElapsedTimeAndWarn(startTime, tag = tag) }
    }

// ----------------------------------------------
fun checkElapsedTimeAndWarn(startTime: Long, tag: String = DEFAULT_TAG) {
    val elapsedTime = System.currentTimeMillis() - startTime
    if (elapsedTime >= BuildConfig.RESPONSE_TIME_THRESHOLD) {
        val message = "Waiting for $tag longer than expected"; Timber.w(message)
        SentryUtil.w(message, listOf("elapsed time" to "$elapsedTime", "threshold" to "${BuildConfig.RESPONSE_TIME_THRESHOLD}"))
    }
}
