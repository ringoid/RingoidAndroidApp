package com.ringoid.utility

import com.ringoid.utility.RxViewUtil.DEBOUNCE_CLICK
import com.ringoid.utility.RxViewUtil.DEBOUNCE_INPUT
import com.ringoid.utility.RxViewUtil.POST_DELAY
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

object RxViewUtil {

    const val DEBOUNCE_CLICK = 125L
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

@Suppress("CheckResult")
fun delay(time: Long = POST_DELAY, units: TimeUnit = TimeUnit.MILLISECONDS, body: () -> Unit) {
    Single.just(0)
        .delay(time, units)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ body() }, Timber::e)
}
