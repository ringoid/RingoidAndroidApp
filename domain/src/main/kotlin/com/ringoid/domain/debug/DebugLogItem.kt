package com.ringoid.domain.debug

import com.ringoid.utility.randomString
import com.ringoid.utility.time
import java.util.*

data class DebugLogItem(
    val id: String = randomString(),
    val log: String = "",
    val level: DebugLogLevel = DebugLogLevel.DEBUG,
    val ts: Long = System.currentTimeMillis()) {

    fun log(): String = "${level.char}|${Date(ts).time()}: $log"
}

val EmptyDebugLogItem = DebugLogItem()
