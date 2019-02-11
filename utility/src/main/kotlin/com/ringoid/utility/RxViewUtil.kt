package com.ringoid.utility

import android.os.Looper
import io.reactivex.ObservableTransformer
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import timber.log.Timber
import java.util.concurrent.TimeUnit

fun <T> clickDebounce(timeout: Long = BuildConfig.DEBOUNCE_CLICK): ObservableTransformer<T, T> =
    ObservableTransformer { it.throttleFirst(timeout, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()) }

fun <T> inputDebounce(timeout: Long = BuildConfig.DEBOUNCE_INPUT): ObservableTransformer<T, T> =
    ObservableTransformer { it.debounce(timeout, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()) }

fun checkMainThread2(observer: Observer<*>): Boolean {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        observer.onSubscribe(Disposables.empty())
        observer.onError(IllegalStateException("Expected to be called on the main thread but was " + Thread.currentThread().name))
        return false
    }
    return true
}

@Suppress("CheckResult")
fun delay(delay: Long = BuildConfig.POST_DELAY, units: TimeUnit = TimeUnit.MILLISECONDS, body: () -> Unit) {
    Single.just(0)
        .delay(delay, units)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ body() }, Timber::e)
}
