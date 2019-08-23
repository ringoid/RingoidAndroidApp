package com.ringoid.origin.model

import android.annotation.TargetApi
import android.os.Build
import android.os.Debug

/**
 * This class is not reusable, to have debug info updates - instantiate this class again.
 */
data class DebugInfoItem(val meminfo: MemInfo, private val threadinfo: ThreadInfo = ThreadInfo()) {

    var threadNumber: Int = 0
        private set
    var threadBlocked: Int = 0
        private set
    var threadRunning: Int = 0
        private set
    var threadWaiting: Int = 0
        private set

    constructor(meminfo: Debug.MemoryInfo): this(meminfo = MemInfo(meminfo))

    init {
        threadNumber = threadinfo.threadNumber
        threadBlocked = threadinfo.threadBlocked
        threadRunning = threadinfo.threadRunning
        threadWaiting = threadinfo.threadWaiting
    }
}

@TargetApi(Build.VERSION_CODES.M)
data class MemInfo(val java: String, val native: String, val code: String) {

    constructor(meminfo: Debug.MemoryInfo)
        : this(java = meminfo.getMemoryStat("summary.java-heap"),
               native = meminfo.getMemoryStat("summary.native-heap"),
               code = meminfo.getMemoryStat("summary.code"))
}

class ThreadInfo {

    private val threadStat by lazy { Thread.getAllStackTraces().keys }

    val threadNumber by lazy { threadStat.size }
    val threadBlocked by lazy { threadStat.count { it.state == Thread.State.BLOCKED } }
    val threadRunning by lazy { threadStat.count { it.state == Thread.State.RUNNABLE } }
    val threadWaiting by lazy { threadStat.count { it.state == Thread.State.WAITING ||
                                                   it.state == Thread.State.TIMED_WAITING } }
}
