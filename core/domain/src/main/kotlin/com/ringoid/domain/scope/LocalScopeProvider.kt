package com.ringoid.domain.scope

import com.uber.autodispose.lifecycle.CorrespondingEventsFunction
import com.uber.autodispose.lifecycle.LifecycleEndedException
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class LocalScopeProvider @Inject constructor() : LifecycleScopeProvider<LocalLifecycleEvent> {

    companion object {
        private val CORRESPONDING_EVENTS = CorrespondingEventsFunction<LocalLifecycleEvent> { event ->
            when (event) {
                LocalLifecycleEvent.STARTED -> LocalLifecycleEvent.STOPPED
                else -> throw LifecycleEndedException("Local scope has finished.")
            }
        }
    }

    private val lifecycleEvents = BehaviorSubject.createDefault(LocalLifecycleEvent.STARTED)

    override fun lifecycle(): Observable<LocalLifecycleEvent> = lifecycleEvents.hide()

    override fun correspondingEvents(): CorrespondingEventsFunction<LocalLifecycleEvent> = CORRESPONDING_EVENTS

    override fun peekLifecycle(): LocalLifecycleEvent? = lifecycleEvents.value

    fun start() {
        lifecycleEvents.onNext(LocalLifecycleEvent.STARTED)
    }

    fun stop() {
        lifecycleEvents.onNext(LocalLifecycleEvent.STOPPED)
    }
}
