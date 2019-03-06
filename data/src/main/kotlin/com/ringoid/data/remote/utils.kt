package com.ringoid.data.remote

import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.debug.DebugLogLevel
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.model.IEssence
import io.reactivex.*
import okhttp3.MediaType
import okhttp3.RequestBody
import timber.log.Timber

fun IEssence.toBody(): RequestBody = RequestBody.create(MediaType.parse("application/json"), this.toJson())

const val DEFAULT_TAG = "response"

// ------------------------------------------------------------------------------------------------
fun Completable.logRequest(tag: String = "", vararg data: Pair<String, String>): Completable = this  // no-op, for symmetry
inline fun <reified T : BaseResponse> Maybe<T>.logRequest(tag: String = "", vararg data: Pair<String, String>): Maybe<T> = compose(logRequestMaybe<T>(tag, *data))
inline fun <reified T : BaseResponse> Single<T>.logRequest(tag: String = "", vararg data: Pair<String, String>): Single<T> = compose(logRequestSingle<T>(tag, *data))
inline fun <reified T : BaseResponse> Flowable<T>.logRequest(tag: String = "", vararg data: Pair<String, String>): Flowable<T> = compose(logRequestFlowable<T>(tag, *data))
inline fun <reified T : BaseResponse> Observable<T>.logRequest(tag: String = "", vararg data: Pair<String, String>): Observable<T> = compose(logRequestObservable<T>(tag, *data))

inline fun <reified T : BaseResponse> logRequestMaybe(tag: String = "", vararg data: Pair<String, String>): MaybeTransformer<T, T> =
    MaybeTransformer { it.doOnSubscribe { logBaseRequest(tag, *data) } }
inline fun <reified T : BaseResponse> logRequestSingle(tag: String = "", vararg data: Pair<String, String>): SingleTransformer<T, T> =
    SingleTransformer { it.doOnSubscribe { logBaseRequest(tag, *data) } }
inline fun <reified T : BaseResponse> logRequestFlowable(tag: String = "", vararg data: Pair<String, String>): FlowableTransformer<T, T> =
    FlowableTransformer { it.doOnSubscribe { logBaseRequest(tag, *data) } }
inline fun <reified T : BaseResponse> logRequestObservable(tag: String = "", vararg data: Pair<String, String>): ObservableTransformer<T, T> =
    ObservableTransformer { it.doOnSubscribe { logBaseRequest(tag, *data) } }

fun logBaseRequest(tag: String = "", vararg data: Pair<String, String>) {
    DebugLogUtil.i("Request [$tag]: ${data.joinToString()}")
}

// ----------------------------------------------
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
    DebugLogUtil.log("Response [$tag]: ${it.toLogString()} ${it.errorString()}".trim(),
                     level = if (it.isSuccessful()) DebugLogLevel.DEBUG else DebugLogLevel.ERROR)
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
        val message = "Waiting for $tag longer than expected ${BuildConfig.RESPONSE_TIME_THRESHOLD} ms"; Timber.w(message)
        SentryUtil.w(message, listOf("elapsed time" to "$elapsedTime", "threshold" to "${BuildConfig.RESPONSE_TIME_THRESHOLD}"))
    }
}
