package com.ringoid.data.action_storage

import java.util.concurrent.atomic.AtomicInteger

internal class TriggerSemaphore {

    private val triggerInProgress: AtomicInteger = AtomicInteger(0)

    internal fun check(): Boolean = triggerInProgress.get() > 0

    internal fun increment() {
        triggerInProgress.getAndIncrement()
    }

    internal fun decrement() {
        if (triggerInProgress.decrementAndGet() < 0) {
            drop()
        }
    }

    internal fun drop() {
        triggerInProgress.set(0)
    }
}
