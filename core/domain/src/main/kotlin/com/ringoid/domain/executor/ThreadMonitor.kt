package com.ringoid.domain.executor

import com.ringoid.domain.BuildConfig
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.utility.DebugOnly
import timber.log.Timber
import kotlin.math.min

/**
 * Prints useful information about all threads in our process. Information includes
 * [Thread.getName], [Thread.getState] and the stacktrace of the [Thread] up to 10 items.
 *
 * To stop thread call [interrupt].
 *
 * Things to look for:
 *
 * - Too many threads.
 * - Rx IO threads that are not blocking on I/O (see their stack trace).
 * - Computation threads that are not idle (see their stack trace).
 * - Duplicate thread names because thread-pools are not being reused (for example more than one
 *   Glide or root OkHttpClient instance)
 *
 * @see https://gist.github.com/joshallenit/9f9271474d62d206174ae580147e876f
 */
@DebugOnly
class ThreadMonitor constructor(
    private val intervalMs: Long = 30000,  // run every 'intervalMs' ms
    private val maxStack: Int = 10) : Thread("ThreadMonitor") {

    init {
        // Use MAX_PRIORITY so that we can see what other threads are doing without waiting for
        // them to go to idle.
        priority = MAX_PRIORITY
    }

    override fun run() {
        try {
            while (!Thread.currentThread().isInterrupted) {
                if (DomainUtil.withThreadInfo()) {
                    // Get all the threads and sort them by name
                    val threads = getAllStackTraces().toList().sortedBy { (thread, _) -> thread.name }
                    // For each thread, we get the index, name and stacktrace up to 10 items
                    // and log them
                    DebugLogUtil.b("------------------------------------------------------------------------------------")
                    for (i in 0 until threads.size) {
                        val (thread, stacktrace) = threads[i]
                        val message = "Thread status: ${thread.name}[${thread.state}](${i + 1}/${threads.size})"
                        if (BuildConfig.DEBUG) {
                            val stack = stacktrace.toList().subList(0, min(maxStack, stacktrace.size))
                            Timber.v("$message ${stack.joinToString("\n\t\t\t", "\n\t\t\t", "\n-------------------------")}")
                        }
                        DebugLogUtil.d(message)
                    }
                    DebugLogUtil.b("------------------------------------------------------------------------------------")
                }
                sleep(intervalMs)
            }
        } catch (interrupted: InterruptedException) {
            Timber.e(interrupted, "Thread monitor was interrupted")
            // stop 'while' loop
        }
    }
}
