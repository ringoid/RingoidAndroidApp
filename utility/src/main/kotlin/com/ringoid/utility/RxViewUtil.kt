package com.ringoid.utility

import com.ringoid.utility.RxViewUtil.DEBOUNCE_CLICK
import com.ringoid.utility.RxViewUtil.DEBOUNCE_INPUT
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

object RxViewUtil {

    const val DEBOUNCE_CLICK = 125L
    const val DEBOUNCE_INPUT = 175L
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
