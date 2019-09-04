package com.ringoid.debug

import android.util.Log

enum class DebugLogLevel(val char: Char, val priority: Int) {
    LIFECYCLE('L', Log.VERBOSE),
    BUS('B', Log.DEBUG),
    VERBOSE('V', Log.VERBOSE),
    DEBUG('D', Log.DEBUG),
    DEBUG2('D', Log.DEBUG),
    INFO('I', Log.INFO),
    WARNING('W', Log.WARN),
    ERROR('E', Log.ERROR)
}
