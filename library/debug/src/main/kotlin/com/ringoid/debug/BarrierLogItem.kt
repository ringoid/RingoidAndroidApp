package com.ringoid.debug

data class BarrierLogItem(
    val log: String,
    val threadId: Long = Thread.currentThread().id,
    val threadName: String = Thread.currentThread().name,
    val ts: Long = System.currentTimeMillis()) {

    override fun toString(): String = "[$threadName($threadId)]: $log ;; $ts"
}
