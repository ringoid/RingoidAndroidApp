package com.ringoid.domain.debug

import com.ringoid.utility.randomString

data class DebugLogItem(
    val id: String = randomString(),
    val log: String = "",
    val level: DebugLogLevel = DebugLogLevel.DEBUG,
    val ts: Long = System.currentTimeMillis())

val EmptyDebugLogItem = DebugLogItem()
