package com.ringoid.data.remote

import com.ringoid.domain.BuildConfig
import com.ringoid.domain.SentryUtil
import com.ringoid.domain.model.IEssence
import io.reactivex.*
import okhttp3.MediaType
import okhttp3.RequestBody
import timber.log.Timber

fun IEssence.toBody(): RequestBody = RequestBody.create(MediaType.parse("application/json"), this.toJson())

const val DEFAULT_TAG = "response"

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

// ------------------------------------------------------------------------------------------------
fun checkElapsedTimeAndWarn(startTime: Long, tag: String = DEFAULT_TAG) {
    val elapsedTime = System.currentTimeMillis() - startTime
    if (elapsedTime >= BuildConfig.REQUEST_TIME_THRESHOLD) {
        val message = "Waiting for $tag longer than expected"; Timber.w(message)
        SentryUtil.w(message, listOf("elapsed time" to "$elapsedTime", "threshold" to "${BuildConfig.REQUEST_TIME_THRESHOLD}"))
    }
}
