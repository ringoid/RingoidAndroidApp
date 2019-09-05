package com.ringoid.debug.timer

import com.uber.autodispose.lifecycle.CorrespondingEventsFunction
import com.uber.autodispose.lifecycle.LifecycleEndedException
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

internal class TimeKeeperScopeProvider : LifecycleScopeProvider<TimeKeeperLifecycleEvent> {

    companion object {
        private val CORRESPONDING_EVENTS = CorrespondingEventsFunction<TimeKeeperLifecycleEvent> { event ->
            when (event) {
                TimeKeeperLifecycleEvent.START -> TimeKeeperLifecycleEvent.STOP
                else -> throw LifecycleEndedException("Cannot bind to TimeKeeper scope lifecycle after stopped.")
            }
        }
    }

    private val lifecycleEvents = BehaviorSubject.createDefault(TimeKeeperLifecycleEvent.START)

    override fun lifecycle(): Observable<TimeKeeperLifecycleEvent> = lifecycleEvents.hide()

    override fun correspondingEvents(): CorrespondingEventsFunction<TimeKeeperLifecycleEvent> = CORRESPONDING_EVENTS

    override fun peekLifecycle(): TimeKeeperLifecycleEvent? = lifecycleEvents.value

    fun onStart() {
        lifecycleEvents.onNext(TimeKeeperLifecycleEvent.START)
    }

    fun onStop() {
        lifecycleEvents.onNext(TimeKeeperLifecycleEvent.STOP)
    }
}
