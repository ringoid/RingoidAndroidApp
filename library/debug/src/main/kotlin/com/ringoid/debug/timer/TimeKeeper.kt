package com.ringoid.debug.timer

import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class TimeKeeper(private val interval: Long = TIME_KEEP_INTERVAL) {

    companion object {
        const val TIME_KEEP_INTERVAL = 4L  // seconds
    }

    private var callback: (() -> Unit)? = null
    private val scopeProvider = TimeKeeperScopeProvider()
    private var ticks: Int = 0

    fun registerCallback(callback: () -> Unit) {
        this.callback = callback
    }

    fun unregisterCallback() {
        this.callback = null
    }

    fun start() {
        Observable.interval(interval, TimeUnit.SECONDS)
            .doOnSubscribe { scopeProvider.onStart(); Timber.v("Time keeper has started") }
            .doOnNext { ++ticks; Timber.v("Time keeper tick [$ticks], elapsed ${ticks * interval} s") }
            .doOnDispose { Timber.v("Time keeper has stopped and disposed") }
            .autoDisposable(scopeProvider)
            .subscribe({ callback?.invoke() }, Timber::e)
    }

    fun stop() {
        Timber.v("Time keeper has been requested to stop")
        scopeProvider.onStop()
    }
}
