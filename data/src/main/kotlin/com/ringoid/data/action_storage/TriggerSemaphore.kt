package com.ringoid.data.action_storage

import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

internal class TriggerSemaphore {

    private val triggerInProgress: AtomicInteger = AtomicInteger(0)

    internal fun isLocked(): Boolean = triggerInProgress.get() > 0

    internal fun increment() {
        Timber.v("Increment semaphore")
        triggerInProgress.getAndIncrement()
    }

    internal fun decrement() {
        Timber.v("Decrement semaphore")
        if (triggerInProgress.decrementAndGet() < 0) {
            drop()
        }
    }

    internal fun drop() {
        Timber.v("Drop semaphore")
        triggerInProgress.set(0)
    }
}
