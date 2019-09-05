package com.ringoid.debug.barrier

import com.ringoid.debug.DebugLogLevel
import com.ringoid.debug.DebugLogUtil
import com.ringoid.utility.DebugOnly
import java.util.concurrent.atomic.AtomicBoolean

@DebugOnly
object SimpleBarrierLogUtil {

    private var isEnabled = AtomicBoolean(false)

    private val headers = ArrayList<String>()
    private val footers = ArrayList<String>()
    private val logs = ArrayList<String>(1000)

    internal fun isEnabled(): Boolean = isEnabled.get()

    @Synchronized
    fun enable(msg: String) {
        isEnabled.set(true)
        headers.add("-=== Thread ${Thread.currentThread().name} [id=${Thread.currentThread().id}]: $msg ===-")
    }

    @Synchronized
    fun disable(msg: String) {
        isEnabled.set(false)
        footers.add("-=== Thread ${Thread.currentThread().name} [id=${Thread.currentThread().id}]: $msg ===-")
        logs.clear()
    }

    fun log(log: String) {
        logs.add(log)
    }

    fun getDebugLog(): List<Pair<String, DebugLogLevel>> {
        val l = mutableListOf<Pair<String, DebugLogLevel>>()
        var position = 0
        val size = minOf(headers.size, footers.size)
        for (i in 0 until size) {
            l.add(headers[i] to DebugLogLevel.DEBUG)
            l.add(footers[i] to DebugLogLevel.DEBUG)
            ++position
        }
        if (headers.size > position) {
            headers.subList(position, headers.size).forEach { l.add(it to DebugLogLevel.DEBUG) }
        }
        if (footers.size > position) {
            footers.subList(position, footers.size).forEach { l.add(it to DebugLogLevel.DEBUG) }
        }
        logs.forEach { l.add(it to DebugLogLevel.VERBOSE) }
        return l
    }

    fun printDebugLog() {
        getDebugLog().forEach { (log, level) -> DebugLogUtil.log(log, level) }
    }
}
