package com.ringoid.data

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.ringoid.datainterface.remote.model.BaseResponse
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.BuildConfig
import com.ringoid.report.exception.*
import com.ringoid.report.log.Report
import com.ringoid.report.log.ReportLevel
import io.reactivex.*
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import timber.log.Timber
import java.lang.Math.pow
import java.util.concurrent.TimeUnit

/* Retry with exponential backoff */
// ------------------------------------------------------------------------------------------------
fun Completable.withRetry(
    count: Int = BuildConfig.DEFAULT_RETRY_COUNT,
    delay: Long = BuildConfig.DEFAULT_RETRY_DELAY,
    tag: String? = null, trace: Trace? = null,
    extraTraces: Collection<Trace> = emptyList()): Completable =
        doOnError { Timber.w("Retry [$tag] on error $count") }
        .compose(expBackoffCompletable(count = count, delay = delay, tag = tag, trace = trace, extraTraces = extraTraces))

inline fun <reified T : BaseResponse> Maybe<T>.withRetry(
    count: Int = BuildConfig.DEFAULT_RETRY_COUNT,
    delay: Long = BuildConfig.DEFAULT_RETRY_DELAY,
    tag: String? = null, trace: Trace? = null,
    extraTraces: Collection<Trace> = emptyList()): Maybe<T> =
        doOnError { Timber.w("Retry [$tag] on error $count") }
        .compose(expBackoffMaybe(count = count, delay = delay, tag = tag, trace = trace, extraTraces = extraTraces))

inline fun <reified T : BaseResponse> Single<T>.withRetry(
    count: Int = BuildConfig.DEFAULT_RETRY_COUNT,
    delay: Long = BuildConfig.DEFAULT_RETRY_DELAY,
    tag: String? = null, trace: Trace? = null,
    extraTraces: Collection<Trace> = emptyList()): Single<T> =
        doOnError { Timber.w("Retry [$tag] up to $count times on error [${it.javaClass.simpleName}: ${it.message}]") }
        .compose(expBackoffSingle(count = count, delay = delay, tag = tag, trace = trace, extraTraces = extraTraces))

inline fun <reified T : BaseResponse> Flowable<T>.withRetry(
    count: Int = BuildConfig.DEFAULT_RETRY_COUNT,
    delay: Long = BuildConfig.DEFAULT_RETRY_DELAY,
    tag: String? = null, trace: Trace? = null,
    extraTraces: Collection<Trace> = emptyList()): Flowable<T> =
        doOnError { Timber.w("Retry [$tag] on error $count") }
        .compose(expBackoffFlowable(count = count, delay = delay, tag = tag, trace = trace, extraTraces = extraTraces))

inline fun <reified T : BaseResponse> Observable<T>.withRetry(
    count: Int = BuildConfig.DEFAULT_RETRY_COUNT,
    delay: Long = BuildConfig.DEFAULT_RETRY_DELAY,
    tag: String? = null, trace: Trace? = null,
    extraTraces: Collection<Trace> = emptyList()): Observable<T> =
        doOnError { Timber.w("Retry [$tag] on error $count") }
        .compose(expBackoffObservable(count = count, delay = delay, tag = tag, trace = trace, extraTraces = extraTraces))

// ----------------------------------------------
private fun expBackoffFlowableImpl(
        count: Int, delay: Long, elapsedTimes: MutableList<Long>,
        tag: String? = null, trace: Trace? = null,
        extraTraces: Collection<Trace> = emptyList()) =
    { it: Flowable<Throwable> ->
        it.zipWith<Int, Pair<Int, Throwable>>(Flowable.range(1, count), BiFunction { e: Throwable, i -> i to e })
            .flatMap { (attemptNumber, error) ->
                var exception: Throwable? = null
                val extras = tag?.let { listOf("tag" to "$tag [${error.javaClass.simpleName}]") }
                val delayTime = when (error) {
                    is RepeatRequestAfterSecException -> {
                        val elapsedTime = elapsedTimes.takeIf { it.isNotEmpty() }?.let { it.reduce { acc, l -> acc + l } } ?: 0L
                        if ((error.delay + elapsedTime) > BuildConfig.REQUEST_TIME_THRESHOLD) {
                            "Repeat after delay exceeded time threshold".let { msg ->
                                DebugLogUtil.e(error, message = msg, tag = tag)
                                Report.capture(error, message = "$msg ${BuildConfig.REQUEST_TIME_THRESHOLD} ms",
                                               level = ReportLevel.WARNING, tag = tag, extras = extras)
                            }
                            exception = ThresholdExceededException()  // abort retry and fallback
                        }
                        elapsedTimes.add(error.delay)
                        error.delay  // delay in ms
                    }
                    // don't retry on fatal network errors
                    is ModelNotFoundException,
                    is InvalidAccessTokenApiException,
                    is OldAppVersionApiException,
                    is WrongRequestParamsClientApiException -> {
                        DebugLogUtil.e(error, message = error.message ?: "", tag = tag)
                        Report.capture(error, message = error.message, tag = tag, extras = extras)
                        exception = error  // abort retry and fallback
                        0L  // delay in ms
                    }
                    is SilentFatalException -> {
                        exception = error; 0L  // fallback silently w/o Sentry alert
                    }
                    is NetworkUnexpected -> {
                        when (error.code) {
                            NetworkUnexpected.ERROR_CONNECTION_INSECURE -> delay * attemptNumber * 2  // linear delay
                            else -> {
                                DebugLogUtil.e(error, message = error.message ?: "", tag = tag)
                                Report.capture(error, message = error.message, tag = tag, extras = extras)
                                exception = error  // abort retry and fallback
                                0L  // delay in ms
                            }
                        }
                    }
                    else -> delay * pow(1.8, attemptNumber.toDouble()).toLong()  // exponential delay
                }
                if (tag?.equals("commitActions") != true && delay > BuildConfig.REQUEST_TIME_THRESHOLD) {
                    /**
                     * Exponential delay exceeds threshold, and this is not 'RepeatRequestAfterSecException'
                     * (because Server-side value for delay is just 800 ms, which is less than threshold).
                     */
                    "Common retry after delay exceeded time threshold".let { msg ->
                        DebugLogUtil.e(error, msg, tag = tag)
                        Report.capture(error, message = "$msg ${BuildConfig.REQUEST_TIME_THRESHOLD} ms")
                    }
                    exception = ThresholdExceededException()  // abort retry and fallback, in common case
                }

                exception?.let { Flowable.error<Long>(it) }
                    ?: Flowable.timer(delayTime, TimeUnit.MILLISECONDS, Schedulers.io())
                            .doOnSubscribe {
                                DebugLogUtil.w("Retry [$tag] [$attemptNumber / $count] on: ${error.message}")
                                Report.breadcrumb("Retry attempt", "tag" to "$tag",
                                    "attemptNumber" to "$attemptNumber", "count" to "$count",
                                    "exception" to error.javaClass.canonicalName, "message" to "${error.message}",
                                    "exception message" to "${error.message}")
                            }
                            .doOnNext {
                                if (error is RepeatRequestAfterSecException) {
//                                  SentryUtil.capture(error, message = "Repeat after delay", level = Event.Level.WARNING, tag = tag, extras = extras)
                                    if (attemptNumber >= 3) {
                                        "Repeat after delay 3+ times in a row".let { msg ->
                                            DebugLogUtil.e(error, message = msg, tag = tag)
                                            Report.capture(error, message = msg, level = ReportLevel.WARNING,
                                                           tag = tag, extras = extras)
                                        }
                                    }
                                    trace?.incrementMetric("repeatRequestAfter", 1L)
                                    extraTraces.forEach { it.incrementMetric("repeatRequestAfter", 1L) }
                                } else {
                                    trace?.incrementMetric("retry", 1L)
                                    extraTraces.forEach { it.incrementMetric("retry", 1L) }
                                }
                            }
                            .flatMap {
                                if (attemptNumber >= count) {
                                    trace?.putAttribute("result", "failed")
                                    extraTraces.forEach { trace -> trace.putAttribute("result", "failed") }
                                    "Failed to retry: all attempts have exhausted".let { msg ->
                                        DebugLogUtil.e(error, message = msg, tag = tag)
                                        Report.capture(error, message = msg, tag = tag, extras = extras)
                                    }
                                    Flowable.error<Long>(error)
                                } else Flowable.just(it)
                            }
            }
    }

fun expBackoffCompletable(count: Int, delay: Long, tag: String? = null, trace: Trace? = null,
                          extraTraces: Collection<Trace> = emptyList()): CompletableTransformer =
    CompletableTransformer {
        val elapsedTimes = mutableListOf<Long>()
        it.retryWhen(expBackoffFlowableImpl(count, delay, elapsedTimes, tag, trace, extraTraces))
    }

fun <T : BaseResponse> expBackoffMaybe(count: Int, delay: Long, tag: String? = null, trace: Trace? = null,
                                       extraTraces: Collection<Trace> = emptyList()): MaybeTransformer<T, T> =
    MaybeTransformer {
        val elapsedTimes = mutableListOf<Long>()
        it.retryWhen(expBackoffFlowableImpl(count, delay, elapsedTimes, tag, trace, extraTraces))
    }

fun <T : BaseResponse> expBackoffSingle(count: Int, delay: Long, tag: String? = null, trace: Trace? = null,
                                        extraTraces: Collection<Trace> = emptyList()): SingleTransformer<T, T> =
    SingleTransformer {
        val elapsedTimes = mutableListOf<Long>()
        it.retryWhen(expBackoffFlowableImpl(count, delay, elapsedTimes, tag, trace, extraTraces))
    }

fun <T : BaseResponse> expBackoffFlowable(count: Int, delay: Long, tag: String? = null, trace: Trace? = null,
                                          extraTraces: Collection<Trace> = emptyList()): FlowableTransformer<T, T> =
    FlowableTransformer {
        val elapsedTimes = mutableListOf<Long>()
        it.retryWhen(expBackoffFlowableImpl(count, delay, elapsedTimes, tag, trace, extraTraces))
    }

fun <T : BaseResponse> expBackoffObservable(count: Int, delay: Long, tag: String? = null, trace: Trace? = null,
                                            extraTraces: Collection<Trace> = emptyList()): ObservableTransformer<T, T> =
    ObservableTransformer {
        val elapsedTimes = mutableListOf<Long>()
        it.toFlowable(BackpressureStrategy.MISSING)
          .retryWhen(expBackoffFlowableImpl(count, delay, elapsedTimes, tag, trace, extraTraces))
          .toObservable()
    }

/* Api Error */
// --------------------------------------------------------------------------------------------
/** Completable.withApiError() cannot be provided because [BaseResponse] is required to extract [BaseResponse.errorCode],
 *  which is not the case for [Completable] responses due to lack of response body. So no API error is that case. */
inline fun <reified T : BaseResponse> Maybe<T>.withApiError(tag: String? = null): Maybe<T> =
    flatMap { it.checkApiError(tag)?.let { e -> Maybe.error<T>(e)} ?: Maybe.just(it) }
inline fun <reified T : BaseResponse> Single<T>.withApiError(tag: String? = null): Single<T> =
    flatMap { it.checkApiError(tag)?.let { e -> Single.error<T>(e)} ?: Single.just(it) }
inline fun <reified T : BaseResponse> Flowable<T>.withApiError(tag: String? = null): Flowable<T> =
    flatMap { it.checkApiError(tag)?.let { e -> Flowable.error<T>(e)} ?: Flowable.just(it) }
inline fun <reified T : BaseResponse> Observable<T>.withApiError(tag: String? = null): Observable<T> =
    flatMap { it.checkApiError(tag)?.let { e -> Observable.error<T>(e)} ?: Observable.just(it) }

// ----------------------------------------------
inline fun <reified T : BaseResponse> T.checkApiError(tag: String? = null): Throwable? {
    var error: Throwable? = null
    try {
        if (!unexpected.isNullOrBlank()) {
            error = NetworkUnexpected.from(unexpected!!)
        }
        if (!errorCode.isNullOrBlank()) {
            val apiError = when (errorCode) {
                ApiException.OLD_APP_VERSION -> OldAppVersionApiException(message = errorMessage, tag = tag)
                ApiException.INVALID_ACCESS_TOKEN -> InvalidAccessTokenApiException( message = errorMessage, tag = tag)
                ApiException.CLIENT_ERROR, ApiException.CLIENT_PARAM_ERROR_SEX ->
                    WrongRequestParamsClientApiException(message = errorMessage, tag = tag)
                ApiException.SERVER_ERROR -> InternalServerErrorApiException(message = errorMessage, tag = tag)
                else -> ApiException(code = errorCode, message = errorMessage, tag = tag)
            }
            error = apiError
        }
        if (repeatRequestAfter > 0) {
            error = RepeatRequestAfterSecException(delay = repeatRequestAfter)
        }
    } catch (e: Throwable) {
        "Unexpected error during api error parsing".let {
            DebugLogUtil.e(e, it, tag = tag)
            Report.capture(e, it, tag = tag, extras = listOf("response" to toString()))
        }
        error = e
    }
    return error
}

/* Network Error */
// --------------------------------------------------------------------------------------------
fun Completable.withNetError(tag: String? = null): Completable =
    compose(onNetErrorCompletable(tag))
inline fun <reified T : BaseResponse> Maybe<T>.withNetError(tag: String? = null): Maybe<T> =
    compose(onNetErrorMaybe(tag))
inline fun <reified T : BaseResponse> Single<T>.withNetError(tag: String? = null): Single<T> =
    compose(onNetErrorSingle(tag))
inline fun <reified T : BaseResponse> Flowable<T>.withNetError(tag: String? = null): Flowable<T> =
    compose(onNetErrorFlowable(tag))
inline fun <reified T : BaseResponse> Observable<T>.withNetError(tag: String? = null): Observable<T> =
    compose(onNetErrorObservable(tag))

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
fun Completable.handleError(
        count: Int = BuildConfig.DEFAULT_RETRY_COUNT,
        delay: Long = BuildConfig.DEFAULT_RETRY_DELAY,
        tag: String? = null, traceTag: String = tag ?: "",
        extraTraces: Collection<Trace> = emptyList()): Completable =
    handleErrorNoRetry(tag)
        .compose {
            val trace = FirebasePerformance.getInstance().newTrace(traceTag)
            val source = if (count > 0) it.withRetry(count = count, delay = delay, tag = tag, trace = trace, extraTraces = extraTraces) else it
            source.doOnSubscribe { trace.start() }.doFinally { trace.stop() }
        }

inline fun <reified T : BaseResponse> Maybe<T>.handleErrorNoRetry(tag: String? = null): Maybe<T> =
    withApiError(tag).withNetError(tag)
inline fun <reified T : BaseResponse> Maybe<T>.handleError(
        count: Int = BuildConfig.DEFAULT_RETRY_COUNT,
        delay: Long = BuildConfig.DEFAULT_RETRY_DELAY,
        tag: String? = null, traceTag: String = tag ?: "",
        extraTraces: Collection<Trace> = emptyList()): Maybe<T> =
    handleErrorNoRetry(tag)
        .compose {
            val trace = FirebasePerformance.getInstance().newTrace(traceTag)
            val source = if (count > 0) it.withRetry(count = count, delay = delay, tag = tag, trace = trace, extraTraces = extraTraces) else it
            source.doOnSubscribe { trace.start() }.doFinally { trace.stop() }
        }

inline fun <reified T : BaseResponse> Single<T>.handleErrorNoRetry(tag: String? = null): Single<T> =
    withApiError(tag).withNetError(tag)
inline fun <reified T : BaseResponse> Single<T>.handleError(
        count: Int = BuildConfig.DEFAULT_RETRY_COUNT,
        delay: Long = BuildConfig.DEFAULT_RETRY_DELAY,
        tag: String? = null, traceTag: String = tag ?: "",
        extraTraces: Collection<Trace> = emptyList()): Single<T> =
    handleErrorNoRetry(tag)
        .compose {
            val trace = FirebasePerformance.getInstance().newTrace(traceTag)
            val source = if (count > 0) it.withRetry(count = count, delay = delay, tag = tag, trace = trace, extraTraces = extraTraces) else it
            source.doOnSubscribe { trace.start() }.doFinally { trace.stop() }
        }
inline fun <reified T : BaseResponse> Single<T>.handleErrorNoTrace(
        count: Int = BuildConfig.DEFAULT_RETRY_COUNT,
        delay: Long = BuildConfig.DEFAULT_RETRY_DELAY,
        tag: String? = null): Single<T> =
    handleErrorNoRetry(tag).compose { if (count > 0) it.withRetry(count = count, delay = delay, tag = tag) else it }

inline fun <reified T : BaseResponse> Flowable<T>.handleErrorNoRetry(tag: String? = null): Flowable<T> =
    withApiError(tag).withNetError(tag)
inline fun <reified T : BaseResponse> Flowable<T>.handleError(
        count: Int = BuildConfig.DEFAULT_RETRY_COUNT,
        delay: Long = BuildConfig.DEFAULT_RETRY_DELAY,
        tag: String? = null, traceTag: String = tag ?: "",
        extraTraces: Collection<Trace> = emptyList()): Flowable<T> =
    handleErrorNoRetry(tag)
        .compose {
            val trace = FirebasePerformance.getInstance().newTrace(traceTag)
            val source = if (count > 0) it.withRetry(count = count, delay = delay, tag = tag, trace = trace, extraTraces = extraTraces) else it
            source.doOnSubscribe { trace.start() }.doFinally { trace.stop() }
        }

inline fun <reified T : BaseResponse> Observable<T>.handleErrorNoRetry(tag: String? = null): Observable<T> =
    withApiError(tag).withNetError(tag)
inline fun <reified T : BaseResponse> Observable<T>.handleError(
        count: Int = BuildConfig.DEFAULT_RETRY_COUNT,
        delay: Long = BuildConfig.DEFAULT_RETRY_DELAY,
        tag: String? = null, traceTag: String = tag ?: "",
        extraTraces: Collection<Trace> = emptyList()): Observable<T> =
    handleErrorNoRetry(tag)
        .compose {
            val trace = FirebasePerformance.getInstance().newTrace(traceTag)
            val source = if (count > 0) it.withRetry(count = count, delay = delay, tag = tag, trace = trace, extraTraces = extraTraces) else it
            source.doOnSubscribe { trace.start() }.doFinally { trace.stop() }
        }
