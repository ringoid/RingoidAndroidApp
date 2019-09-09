package com.ringoid.origin.feed.view.lc

import com.uber.autodispose.lifecycle.CorrespondingEventsFunction
import com.uber.autodispose.lifecycle.LifecycleEndedException
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

internal class LcCoordinatorScopeProvider : LifecycleScopeProvider<LcCoordinatorLifecycleEvent> {

    companion object {
        private val CORRESPONDING_EVENTS = CorrespondingEventsFunction<LcCoordinatorLifecycleEvent> { event ->
            when (event) {
                LcCoordinatorLifecycleEvent.ACTIVE -> LcCoordinatorLifecycleEvent.GONE
                else -> throw LifecycleEndedException("Cannot bind to LcCoordinator scope lifecycle after it's gone.")
            }
        }
    }

    private val lifecycleEvents = BehaviorSubject.createDefault(LcCoordinatorLifecycleEvent.ACTIVE)

    override fun lifecycle(): Observable<LcCoordinatorLifecycleEvent> = lifecycleEvents.hide()

    override fun correspondingEvents(): CorrespondingEventsFunction<LcCoordinatorLifecycleEvent> = CORRESPONDING_EVENTS

    override fun peekLifecycle(): LcCoordinatorLifecycleEvent? = lifecycleEvents.value

    fun activate() {
        lifecycleEvents.onNext(LcCoordinatorLifecycleEvent.ACTIVE)
    }

    fun dispose() {
        lifecycleEvents.onNext(LcCoordinatorLifecycleEvent.GONE)
    }
}
