package com.ringoid.utility

import android.os.Looper
import com.ringoid.utility.RxViewUtil.DEBOUNCE_CLICK
import com.ringoid.utility.RxViewUtil.DEBOUNCE_INPUT
import com.ringoid.utility.RxViewUtil.POST_DELAY
import io.reactivex.ObservableTransformer
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import timber.log.Timber
import java.util.concurrent.TimeUnit

object RxViewUtil {

    const val DEBOUNCE_CLICK = 225L
    const val DEBOUNCE_INPUT = 175L
    const val POST_DELAY = 200L
}

fun <T> clickDebounce(timeout: Long = DEBOUNCE_CLICK): ObservableTransformer<T, T> =
    ObservableTransformer { observable ->
        observable.throttleFirst(timeout, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
    }

fun <T> inputDebounce(timeout: Long = DEBOUNCE_INPUT): ObservableTransformer<T, T> =
    ObservableTransformer { observable ->
        observable.debounce(timeout, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
    }

fun checkMainThread2(observer: Observer<*>): Boolean {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        observer.onSubscribe(Disposables.empty())
        observer.onError(IllegalStateException("Expected to be called on the main thread but was " + Thread.currentThread().name))
        return false
    }
    return true
}

@Suppress("CheckResult")
fun delay(time: Long = POST_DELAY, units: TimeUnit = TimeUnit.MILLISECONDS, body: () -> Unit) {
    Single.just(0)
        .delay(time, units)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ body() }, Timber::e)
}
