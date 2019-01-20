package com.ringoid.origin.view.common.visibility_tracker

import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class TrackingBus<T>(val onSuccess: Consumer<T>, val onError: Consumer<Throwable>,
                     val timeout: Long = VIEW_EVENT_START_THRESHOLD) {

    companion object {
        const val VIEW_EVENT_START_THRESHOLD = 50L  // in millis
    }

    private var subs: Disposable? = null
    private val visibilityState = PublishSubject.create<T>()

    fun postViewEvent(state: T) {
        visibilityState.onNext(state)
    }

    fun subscribe() {
        subs = visibilityState
            .subscribeOn(Schedulers.computation())
            .distinctUntilChanged()
            .debounce(timeout, TimeUnit.MILLISECONDS)
            .subscribe(this::onCallback, onError::accept)
    }

    fun unsubscribe() {
        subs?.dispose()
        subs = null
    }

    private fun onCallback(item: T) {
        onSuccess.accept(item)
    }
}

