package com.ringoid.report.log

import android.util.Log
import io.sentry.event.Event

enum class Level(val lvl: Event.Level) {
    VERBOSE(Event.Level.DEBUG),
    DEBUG(Event.Level.DEBUG),
    INFO(Event.Level.INFO),
    WARNING(Event.Level.WARNING),
    ERROR(Event.Level.ERROR),
    FATAL(Event.Level.FATAL);

    fun toLogPriority(): Int =
        when (this) {
            VERBOSE -> Log.VERBOSE
            DEBUG -> Log.DEBUG
            INFO -> Log.INFO
            WARNING -> Log.WARN
            ERROR -> Log.ERROR
            FATAL -> Log.ASSERT
        }
}
