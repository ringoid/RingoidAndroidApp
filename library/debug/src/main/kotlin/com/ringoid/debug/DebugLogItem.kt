package com.ringoid.debug

import com.ringoid.utility.randomString
import com.ringoid.utility.time
import com.ringoid.utility.wrapMillisUnit
import java.util.*

data class DebugLogItem(
    val id: String = randomString(),
    val log: String = "",
    val level: DebugLogLevel = DebugLogLevel.DEBUG,
    val ts: Long = System.currentTimeMillis()) {

    fun log(): String = "${level.char}|${Date(ts).time()}.${wrapMillisUnit(ts % 1000)}: $log"
}

val EmptyDebugLogItem = DebugLogItem()
