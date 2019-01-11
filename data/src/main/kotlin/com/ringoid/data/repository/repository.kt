package com.ringoid.data.repository

import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.domain.exception.ApiException
import com.ringoid.domain.exception.NetworkException
import io.reactivex.*
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import retrofit2.HttpException
import timber.log.Timber
import java.lang.Math.pow
import java.util.concurrent.TimeUnit

const val DEFAULT_RETRY_COUNT = 5
const val DEFAULT_RETRY_DELAY = 55

/* Retry with exponential backoff */
// ------------------------------------------------------------------------------------------------
inline fun <reified T : BaseResponse> Maybe<T>.withRetry(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY): Maybe<T> =
    doOnError { Timber.e(it, "Retry on error") }.compose(expBackoffMaybe(count = count, delay = delay))

inline fun <reified T : BaseResponse> Single<T>.withRetry(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY): Single<T> =
    doOnError { Timber.e(it, "Retry on error") }.compose(expBackoffSingle(count = count, delay = delay))

inline fun <reified T : BaseResponse> Flowable<T>.withRetry(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY): Flowable<T> =
    doOnError { Timber.e(it, "Retry on error") }.compose(expBackoffFlowable(count = count, delay = delay))

inline fun <reified T : BaseResponse> Observable<T>.withRetry(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY): Observable<T> =
    doOnError { Timber.e(it, "Retry on error") }.compose(expBackoffObservable(count = count, delay = delay))

// ----------------------------------------------
private fun expBackoffFlowableImpl(count: Int, delay: Int) =
    { it: Flowable<Throwable> ->
        it.zipWith<Int, Int>(Flowable.range(1, count), BiFunction { t, i -> i })
            .flatMap { retryCount ->
                val delayTime = delay * pow(5.0, retryCount.toDouble()).toLong()
                Flowable.timer(delayTime, TimeUnit.MILLISECONDS)
            }
    }

private fun expBackoffObservableImpl(count: Int, delay: Int) =
    { it: Observable<Throwable> ->
        it.zipWith<Int, Int>(Observable.range(1, count), BiFunction { t, i -> i })
            .flatMap { retryCount ->
                val delayTime = pow(delay.toDouble(), retryCount.toDouble()).toLong()
                Observable.timer(delayTime, TimeUnit.MILLISECONDS)
            }
    }

fun <T : BaseResponse> expBackoffMaybe(count: Int, delay: Int): MaybeTransformer<T, T> =
    MaybeTransformer { it.retryWhen(expBackoffFlowableImpl(count, delay)) }

fun <T : BaseResponse> expBackoffSingle(count: Int, delay: Int): SingleTransformer<T, T> =
    SingleTransformer { it.retryWhen(expBackoffFlowableImpl(count, delay)) }

fun <T : BaseResponse> expBackoffFlowable(count: Int, delay: Int): FlowableTransformer<T, T> =
    FlowableTransformer { it.retryWhen(expBackoffFlowableImpl(count, delay)) }

fun <T : BaseResponse> expBackoffObservable(count: Int, delay: Int): ObservableTransformer<T, T> =
    ObservableTransformer { it.retryWhen(expBackoffObservableImpl(count, delay)) }

/* Api Error */
// --------------------------------------------------------------------------------------------
inline fun <reified T : BaseResponse> Maybe<T>.withApiError(): Maybe<T> = compose(onApiErrorMaybe())
inline fun <reified T : BaseResponse> Single<T>.withApiError(): Single<T> = compose(onApiErrorSingle())
inline fun <reified T : BaseResponse> Flowable<T>.withApiError(): Flowable<T> = compose(onApiErrorFlowable())
inline fun <reified T : BaseResponse> Observable<T>.withApiError(): Observable<T> = compose(onApiErrorObservable())

// ----------------------------------------------
private fun <T : BaseResponse> onApiErrorConsumer(): Consumer<in T> =
    Consumer {
        it.takeIf { it.errorCode.isNotBlank() }
          ?.let { throw ApiException(code = it.errorCode, message = it.errorMessage) }
    }

fun <T : BaseResponse> onApiErrorMaybe(): MaybeTransformer<T, T> =
    MaybeTransformer { it.doOnSuccess(onApiErrorConsumer()) }

fun <T : BaseResponse> onApiErrorSingle(): SingleTransformer<T, T> =
    SingleTransformer { it.doOnSuccess(onApiErrorConsumer()) }

fun <T : BaseResponse> onApiErrorFlowable(): FlowableTransformer<T, T> =
    FlowableTransformer { it.doOnNext(onApiErrorConsumer()) }

fun <T : BaseResponse> onApiErrorObservable(): ObservableTransformer<T, T> =
    ObservableTransformer { it.doOnNext(onApiErrorConsumer()) }

/* Network Error */
// --------------------------------------------------------------------------------------------
inline fun <reified T : BaseResponse> Maybe<T>.withNetError(): Maybe<T> = compose(onNetErrorMaybe())
inline fun <reified T : BaseResponse> Single<T>.withNetError(): Single<T> = compose(onNetErrorSingle())
inline fun <reified T : BaseResponse> Flowable<T>.withNetError(): Flowable<T> = compose(onNetErrorFlowable())
inline fun <reified T : BaseResponse> Observable<T>.withNetError(): Observable<T> = compose(onNetErrorObservable())

// ----------------------------------------------
inline fun <reified T : BaseResponse> onNetErrorMaybe(): MaybeTransformer<T, T> =
    MaybeTransformer {
        it.onErrorResumeNext { e: Throwable ->
            when (e) {
                is HttpException -> Maybe.error(NetworkException(code = e.code()))
                else -> Maybe.error(e)  // including ApiException
            }
        }
    }

inline fun <reified T : BaseResponse> onNetErrorSingle(): SingleTransformer<T, T> =
    SingleTransformer {
        it.onErrorResumeNext { e: Throwable ->
            when (e) {
                is HttpException -> Single.error(NetworkException(code = e.code()))
                else -> Single.error(e)  // including ApiException
            }
        }
    }

inline fun <reified T : BaseResponse> onNetErrorFlowable(): FlowableTransformer<T, T> =
    FlowableTransformer {
        it.onErrorResumeNext { e: Throwable ->
            when (e) {
                is HttpException -> Flowable.error(NetworkException(code = e.code()))
                else -> Flowable.error(e)  // including ApiException
            }
        }
    }

inline fun <reified T : BaseResponse> onNetErrorObservable(): ObservableTransformer<T, T> =
    ObservableTransformer {
        it.onErrorResumeNext { e: Throwable ->
            when (e) {
                is HttpException -> Observable.error(NetworkException(code = e.code()))
                else -> Observable.error(e)  // including ApiException
            }
        }
    }

/* Error handling */
// --------------------------------------------------------------------------------------------
inline fun <reified T : BaseResponse> Maybe<T>.handleError(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY): Maybe<T> =
    withApiError().withNetError().withRetry(count = count, delay = delay)

inline fun <reified T : BaseResponse> Single<T>.handleError(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY): Single<T> =
    withApiError().withNetError().withRetry(count = count, delay = delay)

inline fun <reified T : BaseResponse> Flowable<T>.handleError(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY): Flowable<T> =
    withApiError().withNetError().withRetry(count = count, delay = delay)

inline fun <reified T : BaseResponse> Observable<T>.handleError(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY): Observable<T> =
    withApiError().withNetError().withRetry(count = count, delay = delay)
