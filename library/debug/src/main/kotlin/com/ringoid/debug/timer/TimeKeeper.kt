package com.ringoid.debug.timer

import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class TimeKeeper(private val interval: Long = TIME_KEEP_INTERVAL) {

    companion object {
        const val TIME_KEEP_INTERVAL = 4L  // seconds
    }

    private var callback: (() -> Unit)? = null
    private val scopeProvider = TimeKeeperScopeProvider()
    private var ticks: Int = 0
    private val startBit = AtomicBoolean(false)
    private val requestedToStopBit = AtomicBoolean(false)

    fun registerCallback(callback: () -> Unit) {
        this.callback = callback
    }

    fun unregisterCallback() {
        this.callback = null
    }

    fun start() {
        if (startBit.get()) {
            if (requestedToStopBit.get()) {
                Timber.e("Time keeper has been requested to stop, but it didn't stop yet")
            } else {
                Timber.w("Time keeper has already started. Somebody tries to start it again")
            }
            return
        }
        startBit.set(true)
        requestedToStopBit.set(false)

        Observable.interval(interval, TimeUnit.SECONDS)
            .doOnSubscribe { scopeProvider.onStart(); Timber.v("Time keeper has started") }
            .doOnNext { ++ticks; Timber.v("Time keeper tick [$ticks], elapsed ${ticks * interval} s") }
            .doOnDispose {
                Timber.v("Time keeper has stopped and disposed")
                startBit.set(false)  // source is infinite, so only dispose will release time keeper
            }
            .autoDisposable(scopeProvider)
            .subscribe({ callback?.invoke() }, Timber::e)
    }

    fun stop() {
        Timber.v("Time keeper has been requested to stop")
        requestedToStopBit.set(true)
        scopeProvider.onStop()
    }
}
