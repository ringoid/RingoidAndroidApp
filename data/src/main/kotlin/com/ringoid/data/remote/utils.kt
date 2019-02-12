package com.ringoid.data.remote

import com.ringoid.domain.BuildConfig
import com.ringoid.domain.SentryUtil
import com.ringoid.domain.model.IEssence
import io.reactivex.*
import okhttp3.MediaType
import okhttp3.RequestBody
import timber.log.Timber

fun IEssence.toBody(): RequestBody = RequestBody.create(MediaType.parse("application/json"), this.toJson())

fun Completable.checkResponseTime(): Completable = compose(checkResponseTimeCompletable())
inline fun <reified T> Maybe<T>.checkResponseTime(): Maybe<T> = compose(checkResponseTimeMaybe())
inline fun <reified T> Single<T>.checkResponseTime(): Single<T> = compose(checkResponseTimeSingle())
inline fun <reified T> Flowable<T>.checkResponseTime(): Flowable<T> = compose(checkResponseTimeFlowable())
inline fun <reified T> Observable<T>.checkResponseTime(): Observable<T> = compose(checkResponseTimeObservable())

fun checkResponseTimeCompletable(): CompletableTransformer =
    CompletableTransformer {
        var startTime = 0L
        it
            .doOnSubscribe { startTime = System.currentTimeMillis() }
            .doFinally { checkElapsedTimeAndWarn(startTime) }
    }

inline fun <reified T> checkResponseTimeMaybe(): MaybeTransformer<T, T> =
    MaybeTransformer {
        var startTime = 0L
        it
            .doOnSubscribe { startTime = System.currentTimeMillis() }
            .doFinally { checkElapsedTimeAndWarn(startTime) }
    }

inline fun <reified T> checkResponseTimeSingle(): SingleTransformer<T, T> =
    SingleTransformer {
        var startTime = 0L
        it
            .doOnSubscribe { startTime = System.currentTimeMillis() }
            .doFinally { checkElapsedTimeAndWarn(startTime) }
    }

inline fun <reified T> checkResponseTimeFlowable(): FlowableTransformer<T, T> =
    FlowableTransformer {
        var startTime = 0L
        it
            .doOnSubscribe { startTime = System.currentTimeMillis() }
            .doFinally { checkElapsedTimeAndWarn(startTime) }
    }

inline fun <reified T> checkResponseTimeObservable(): ObservableTransformer<T, T> =
    ObservableTransformer {
        var startTime = 0L
        it
            .doOnSubscribe { startTime = System.currentTimeMillis() }
            .doFinally { checkElapsedTimeAndWarn(startTime) }
    }

// ------------------------------------------------------------------------------------------------
fun checkElapsedTimeAndWarn(startTime: Long) {
    val elapsedTime = System.currentTimeMillis() - startTime
    if (elapsedTime >= BuildConfig.REQUEST_TIME_THRESHOLD) {
        val message = "Waiting for response longer than expected"; Timber.w(message)
        SentryUtil.w(message, listOf("elapsed time" to "$elapsedTime", "threshold" to "${BuildConfig.REQUEST_TIME_THRESHOLD}"))
    }
}
