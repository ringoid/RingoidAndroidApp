package com.ringoid.debug.model

import com.ringoid.debug.DebugLogLevel
import com.ringoid.debug.DebugLogUtil.GLOBAL_TICK
import com.ringoid.utility.randomString
import com.ringoid.utility.time
import com.ringoid.utility.wrapMillisUnit
import java.util.*

data class DebugLogItem(
    val id: String = randomString(),
    val log: String = "",
    val level: DebugLogLevel = DebugLogLevel.DEBUG,
    val ts: Long = System.currentTimeMillis(),
    val tick: Long = GLOBAL_TICK++) {

    fun log(): String = "${level.char}|${Date(ts).time()}.${wrapMillisUnit(ts % 1000)}~$tick: $log"
}

val EmptyDebugLogItem = DebugLogItem(tick = 0)
