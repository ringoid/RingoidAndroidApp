package com.ringoid.data.action_storage

import java.util.concurrent.atomic.AtomicInteger

class TriggerSemaphore {

    private val triggerInProgress: AtomicInteger = AtomicInteger(0)

    fun check(): Boolean = triggerInProgress.get() > 0

    fun increment() {
        triggerInProgress.getAndIncrement()
    }

    fun decrement() {
        if (triggerInProgress.decrementAndGet() < 0) {
            drop()
        }
    }

    fun drop() {
        triggerInProgress.set(0)
    }
}
