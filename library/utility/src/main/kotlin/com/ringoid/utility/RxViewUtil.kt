package com.ringoid.utility

import android.os.Looper
import android.view.View
import io.reactivex.ObservableTransformer
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

fun <T> clickDebounce(timeout: Long = BuildConfig.DEBOUNCE_CLICK): ObservableTransformer<T, T> =
    ObservableTransformer { it.throttleFirst(timeout, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()) }

fun <T> inputDebounce(timeout: Long = BuildConfig.DEBOUNCE_INPUT): ObservableTransformer<T, T> =
    ObservableTransformer { it.debounce(timeout, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()) }

fun <T> inputDebounceNet(): ObservableTransformer<T, T> =
    inputDebounce(BuildConfig.DEBOUNCE_INPUT_NETWORK)

fun checkMainThread2(): Boolean = Looper.myLooper() == Looper.getMainLooper()

fun checkMainThread2(observer: Observer<*>): Boolean {
    if (!checkMainThread2()) {
        observer.onSubscribe(Disposables.empty())
        observer.onError(IllegalStateException("Expected to be called on the main thread but was " + Thread.currentThread().name))
        return false
    }
    return true
}

fun delay(delay: Long = BuildConfig.POST_DELAY, units: TimeUnit = TimeUnit.MILLISECONDS, body: () -> Unit) {
    delay(delay, units, AndroidSchedulers.mainThread(), body)
}

@Suppress("CheckResult")
fun delay(delay: Long = BuildConfig.POST_DELAY, units: TimeUnit = TimeUnit.MILLISECONDS, scheduler: Scheduler, body: () -> Unit) {
    Single.just(0)
        .delay(delay, units, scheduler)
        .subscribe({ body() }, Timber::e)
}

fun View.delay(delay: Long = BuildConfig.POST_DELAY, body: View.() -> Unit) {
    postDelayed({ body() }, delay)
}

@Suppress("CheckResult")
fun runOnUiThread(body: () -> Unit) {
    Single.just(0)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ body() }, Timber::e)
}

fun thread(runnable: () -> Unit) {
    Thread(runnable).start()
}

fun thread(delay: Long = BuildConfig.POST_DELAY, units: TimeUnit = TimeUnit.MILLISECONDS, runnable: () -> Unit) {
    thread { delay(delay = delay, units = units, scheduler = Schedulers.trampoline(), body = runnable) }
}
