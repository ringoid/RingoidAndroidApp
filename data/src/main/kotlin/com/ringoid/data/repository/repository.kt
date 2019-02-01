package com.ringoid.data.repository

import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.domain.exception.ApiException
import com.ringoid.domain.exception.NetworkException
import com.ringoid.domain.exception.RepeatRequestAfterSecException
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
fun Completable.withRetry(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Completable =
    doOnError { Timber.w("Retry on error $count") }.compose(expBackoffCompletable(count = count, delay = delay, tag = tag))

inline fun <reified T : BaseResponse> Maybe<T>.withRetry(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Maybe<T> =
    doOnError { Timber.w("Retry on error $count") }.compose(expBackoffMaybe(count = count, delay = delay, tag = tag))

inline fun <reified T : BaseResponse> Single<T>.withRetry(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Single<T> =
    doOnError { Timber.w("Retry on error $count") }.compose(expBackoffSingle(count = count, delay = delay, tag = tag))

inline fun <reified T : BaseResponse> Flowable<T>.withRetry(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Flowable<T> =
    doOnError { Timber.w("Retry on error $count") }.compose(expBackoffFlowable(count = count, delay = delay, tag = tag))

inline fun <reified T : BaseResponse> Observable<T>.withRetry(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Observable<T> =
    doOnError { Timber.w(it, "Retry on error $count") }.compose(expBackoffObservable(count = count, delay = delay, tag = tag))

// ----------------------------------------------
private fun expBackoffFlowableImpl(count: Int, delay: Int, tag: String? = null) =
    { it: Flowable<Throwable> ->
        it.zipWith<Int, Pair<Int, Throwable>>(Flowable.range(1, count), BiFunction { e: Throwable, i -> i to e })
            .flatMap { errorWithAttempt ->
                val attemptNumber = errorWithAttempt.first
                val error = errorWithAttempt.second
                val delayTime = when (error) {
                    is RepeatRequestAfterSecException -> error.delay * 1000  // in secons
                    else -> delay * pow(5.0, attemptNumber.toDouble()).toLong()
                }
                Flowable.timer(delayTime, TimeUnit.MILLISECONDS)
                                .doOnComplete {
                                    Timber.e("Retry attempt [$attemptNumber / $count] after error: $error")
                                    if (attemptNumber >= count) throw error
                                }
            }
    }

private fun expBackoffObservableImpl(count: Int, delay: Int, tag: String? = null) =
    { it: Observable<Throwable> ->
        it.zipWith<Int, Pair<Int, Throwable>>(Observable.range(1, count), BiFunction { e: Throwable, i -> i to e })
            .flatMap { errorWithAttempt ->
                val attemptNumber = errorWithAttempt.first
                val error = errorWithAttempt.second
                val delayTime = when (error) {
                    is RepeatRequestAfterSecException -> error.delay * 1000  // in secons
                    else -> delay * pow(5.0, attemptNumber.toDouble()).toLong()
                }
                Observable.timer(delayTime, TimeUnit.MILLISECONDS)
                                  .doOnComplete {
                                      Timber.e("Retry attempt [$attemptNumber / $count] after error: $error")
                                      if (attemptNumber >= count) throw error
                                  }
            }
    }

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
        if (!it.errorCode.isNullOrBlank()) {
            throw ApiException(code = it.errorCode, message = it.errorMessage, tag = tag)
        }
        if (it.repeatAfterSec > 0) {
            throw RepeatRequestAfterSecException(delay = it.repeatAfterSec)
        }
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
fun Completable.handleErrorNoRetry(tag: String? = null): Completable = withNetError(tag)
fun Completable.handleError(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Completable =
    handleErrorNoRetry(tag).compose { if (count > 0) it.withRetry(count = count, delay = delay, tag = tag) else it }

inline fun <reified T : BaseResponse> Maybe<T>.handleErrorNoRetry(tag: String? = null): Maybe<T> =
    withApiError(tag).withNetError(tag)
inline fun <reified T : BaseResponse> Maybe<T>.handleError(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Maybe<T> =
    handleErrorNoRetry(tag).compose { if (count > 0) it.withRetry(count = count, delay = delay, tag = tag) else it }

inline fun <reified T : BaseResponse> Single<T>.handleErrorNoRetry(tag: String? = null): Single<T> =
    withApiError(tag).withNetError(tag)
inline fun <reified T : BaseResponse> Single<T>.handleError(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Single<T> =
    handleErrorNoRetry(tag).compose { if (count > 0) it.withRetry(count = count, delay = delay, tag = tag) else it }

inline fun <reified T : BaseResponse> Flowable<T>.handleErrorNoRetry(tag: String? = null): Flowable<T> =
    withApiError(tag).withNetError(tag)
inline fun <reified T : BaseResponse> Flowable<T>.handleError(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Flowable<T> =
    handleErrorNoRetry(tag).compose { if (count > 0) it.withRetry(count = count, delay = delay, tag = tag) else it }

inline fun <reified T : BaseResponse> Observable<T>.handleErrorNoRetry(tag: String? = null): Observable<T> =
    withApiError(tag).withNetError(tag)
inline fun <reified T : BaseResponse> Observable<T>.handleError(count: Int = DEFAULT_RETRY_COUNT, delay: Int = DEFAULT_RETRY_DELAY, tag: String? = null): Observable<T> =
    handleErrorNoRetry(tag).compose { if (count > 0) it.withRetry(count = count, delay = delay, tag = tag) else it }
