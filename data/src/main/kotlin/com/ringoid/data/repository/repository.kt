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

const val DEFAULT_RETRY_COUNT = 2
const val DEFAULT_RETRY_DELAY = 55

/* Retry with exponential backoff */
// ------------------------------------------------------------------------------------------------
fun Completable.withRetry(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Completable =
    doOnError { Timber.e(it, "Retry on error") }.compose(expBackoffCompletable(count = count, delay = delay, tag = tag))

inline fun <reified T : BaseResponse> Maybe<T>.withRetry(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Maybe<T> =
    doOnError { Timber.e(it, "Retry on error") }.compose(expBackoffMaybe(count = count, delay = delay, tag = tag))

inline fun <reified T : BaseResponse> Single<T>.withRetry(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Single<T> =
    doOnError { Timber.e(it, "Retry on error") }.compose(expBackoffSingle(count = count, delay = delay, tag = tag))

inline fun <reified T : BaseResponse> Flowable<T>.withRetry(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Flowable<T> =
    doOnError { Timber.e(it, "Retry on error") }.compose(expBackoffFlowable(count = count, delay = delay, tag = tag))

inline fun <reified T : BaseResponse> Observable<T>.withRetry(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Observable<T> =
    doOnError { Timber.e(it, "Retry on error") }.compose(expBackoffObservable(count = count, delay = delay, tag = tag))

// ----------------------------------------------
private fun expBackoffFlowableImpl(count: Int, delay: Int, tag: String? = null) =
    { it: Flowable<Throwable> ->
        it.zipWith<Int, Int>(Flowable.range(1, count), BiFunction { _: Throwable, i -> i })
            .flatMap { retryCount ->
                val delayTime = delay * pow(5.0, retryCount.toDouble()).toLong()
                Flowable.timer(delayTime, TimeUnit.MILLISECONDS)
            }
    }  // TODO: handle fail all retries

private fun expBackoffObservableImpl(count: Int, delay: Int, tag: String? = null) =
    { it: Observable<Throwable> ->
        it.zipWith<Int, Int>(Observable.range(1, count), BiFunction { _: Throwable, i -> i })
            .flatMap { retryCount ->
                val delayTime = pow(delay.toDouble(), retryCount.toDouble()).toLong()
                Observable.timer(delayTime, TimeUnit.MILLISECONDS)
            }
    }  // TODO: handle fail all retries

fun expBackoffCompletable(count: Int, delay: Int, tag: String? = null): CompletableTransformer =
    CompletableTransformer { it.retryWhen(expBackoffFlowableImpl(count, delay, tag)) }

fun <T : BaseResponse> expBackoffMaybe(count: Int, delay: Int, tag: String? = null): MaybeTransformer<T, T> =
    MaybeTransformer { it.retryWhen(expBackoffFlowableImpl(count, delay, tag)) }

fun <T : BaseResponse> expBackoffSingle(count: Int, delay: Int, tag: String? = null): SingleTransformer<T, T> =
    SingleTransformer { it.retryWhen(expBackoffFlowableImpl(count, delay, tag)) }

fun <T : BaseResponse> expBackoffFlowable(count: Int, delay: Int, tag: String? = null): FlowableTransformer<T, T> =
    FlowableTransformer { it.retryWhen(expBackoffFlowableImpl(count, delay, tag)) }

fun <T : BaseResponse> expBackoffObservable(count: Int, delay: Int, tag: String? = null): ObservableTransformer<T, T> =
    ObservableTransformer { it.retryWhen(expBackoffObservableImpl(count, delay, tag)) }

/* Api Error */
// --------------------------------------------------------------------------------------------
/** Completable.withApiError() cannot be provided because [BaseResponse] is required to extract [BaseResponse.errorCode],
 *  which is not the case for [Completable] responses due to lack of response body. So no API error is that case. */
inline fun <reified T : BaseResponse> Maybe<T>.withApiError(tag: String? = null): Maybe<T> = compose(onApiErrorMaybe(tag))
inline fun <reified T : BaseResponse> Single<T>.withApiError(tag: String? = null): Single<T> = compose(onApiErrorSingle(tag))
inline fun <reified T : BaseResponse> Flowable<T>.withApiError(tag: String? = null): Flowable<T> = compose(onApiErrorFlowable(tag))
inline fun <reified T : BaseResponse> Observable<T>.withApiError(tag: String? = null): Observable<T> = compose(onApiErrorObservable(tag))

// ----------------------------------------------
private fun <T : BaseResponse> onApiErrorConsumer(tag: String? = null): Consumer<in T> =
    Consumer {
        it.takeIf { !it.errorCode.isNullOrBlank() }
          ?.let { throw ApiException(code = it.errorCode, message = it.errorMessage, tag = tag) }
    }

fun <T : BaseResponse> onApiErrorMaybe(tag: String? = null): MaybeTransformer<T, T> =
    MaybeTransformer { it.doOnSuccess(onApiErrorConsumer(tag)) }

fun <T : BaseResponse> onApiErrorSingle(tag: String? = null): SingleTransformer<T, T> =
    SingleTransformer { it.doOnSuccess(onApiErrorConsumer(tag)) }

fun <T : BaseResponse> onApiErrorFlowable(tag: String? = null): FlowableTransformer<T, T> =
    FlowableTransformer { it.doOnNext(onApiErrorConsumer(tag)) }

fun <T : BaseResponse> onApiErrorObservable(tag: String? = null): ObservableTransformer<T, T> =
    ObservableTransformer { it.doOnNext(onApiErrorConsumer(tag)) }

/* Network Error */
// --------------------------------------------------------------------------------------------
fun Completable.withNetError(tag: String? = null): Completable = compose(onNetErrorCompletable(tag))
inline fun <reified T : BaseResponse> Maybe<T>.withNetError(tag: String? = null): Maybe<T> = compose(onNetErrorMaybe(tag))
inline fun <reified T : BaseResponse> Single<T>.withNetError(tag: String? = null): Single<T> = compose(onNetErrorSingle(tag))
inline fun <reified T : BaseResponse> Flowable<T>.withNetError(tag: String? = null): Flowable<T> = compose(onNetErrorFlowable(tag))
inline fun <reified T : BaseResponse> Observable<T>.withNetError(tag: String? = null): Observable<T> = compose(onNetErrorObservable(tag))

// ----------------------------------------------
fun onNetErrorCompletable(tag: String? = null): CompletableTransformer =
    CompletableTransformer {
        it.onErrorResumeNext { e: Throwable ->
            when (e) {
                is HttpException -> Completable.error(NetworkException(code = e.code(), tag = tag))
                else -> Completable.error(e)  // including ApiException
            }
        }
    }

inline fun <reified T : BaseResponse> onNetErrorMaybe(tag: String? = null): MaybeTransformer<T, T> =
    MaybeTransformer {
        it.onErrorResumeNext { e: Throwable ->
            when (e) {
                is HttpException -> Maybe.error(NetworkException(code = e.code(), tag = tag))
                else -> Maybe.error(e)  // including ApiException
            }
        }
    }

inline fun <reified T : BaseResponse> onNetErrorSingle(tag: String? = null): SingleTransformer<T, T> =
    SingleTransformer {
        it.onErrorResumeNext { e: Throwable ->
            when (e) {
                is HttpException -> Single.error(NetworkException(code = e.code(), tag = tag))
                else -> Single.error(e)  // including ApiException
            }
        }
    }

inline fun <reified T : BaseResponse> onNetErrorFlowable(tag: String? = null): FlowableTransformer<T, T> =
    FlowableTransformer {
        it.onErrorResumeNext { e: Throwable ->
            when (e) {
                is HttpException -> Flowable.error(NetworkException(code = e.code(), tag = tag))
                else -> Flowable.error(e)  // including ApiException
            }
        }
    }

inline fun <reified T : BaseResponse> onNetErrorObservable(tag: String? = null): ObservableTransformer<T, T> =
    ObservableTransformer {
        it.onErrorResumeNext { e: Throwable ->
            when (e) {
                is HttpException -> Observable.error(NetworkException(code = e.code(), tag = tag))
                else -> Observable.error(e)  // including ApiException
            }
        }
    }

/* Error handling */
// --------------------------------------------------------------------------------------------
fun Completable.handleError(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Completable =
    withNetError(tag).withRetry(count = count, delay = delay, tag = tag)

inline fun <reified T : BaseResponse> Maybe<T>.handleError(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Maybe<T> =
    withApiError(tag).withNetError(tag).withRetry(count = count, delay = delay, tag = tag)

inline fun <reified T : BaseResponse> Single<T>.handleError(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Single<T> =
    withApiError(tag).withNetError(tag).withRetry(count = count, delay = delay, tag = tag)

inline fun <reified T : BaseResponse> Flowable<T>.handleError(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Flowable<T> =
    withApiError(tag).withNetError(tag).withRetry(count = count, delay = delay, tag = tag)

inline fun <reified T : BaseResponse> Observable<T>.handleError(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Observable<T> =
    withApiError(tag).withNetError(tag).withRetry(count = count, delay = delay, tag = tag)
