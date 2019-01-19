package com.ringoid.origin.view.common.visibility_tracker

import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class TrackingBus(val onSuccess: Consumer<VisibilityState>, val onError: Consumer<Throwable>,
                  val timeout: Long = VIEW_EVENT_START_THRESHOLD) {

    companion object {
        const val VIEW_EVENT_START_THRESHOLD = 50L  // in millis
    }

    private var subs: Disposable? = null
    private val visibilityState = PublishSubject.create<VisibilityState>()

    fun postViewEvent(state: VisibilityState) {
        visibilityState.onNext(state)
    }

    fun subscribe() {
        subs = visibilityState
            .distinctUntilChanged()
            .throttleWithTimeout(timeout, TimeUnit.MILLISECONDS)
            .subscribe(this::onCallback, onError::accept)
    }

    fun unsubscribe() {
        subs?.dispose()
        subs = null
    }

    private fun onCallback(state: VisibilityState) {
        onSuccess.accept(state)
    }
}

