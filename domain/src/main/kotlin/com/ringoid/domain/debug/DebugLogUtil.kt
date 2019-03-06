package com.ringoid.domain.debug

import com.ringoid.domain.BuildConfig
import io.reactivex.subjects.PublishSubject
import io.sentry.event.Event

@DebugOnly
object DebugLogUtil {

    val logger: PublishSubject<DebugLogItem> = PublishSubject.create()

    fun v(log: String) = log(log, DebugLogLevel.VERBOSE)
    fun d(log: String) = log(log, DebugLogLevel.DEBUG)
    fun i(log: String) = log(log, DebugLogLevel.INFO)
    fun w(log: String) = log(log, DebugLogLevel.WARNING)
    fun e(log: String) = log(log, DebugLogLevel.ERROR)
    fun e(e: Throwable) = log(log = "${e.javaClass.simpleName}: ${e.message}".trim(), level = DebugLogLevel.ERROR)

    fun log(log: String, level: Event.Level) {
        when (level) {
            Event.Level.DEBUG -> d(log)
            Event.Level.INFO -> i(log)
            Event.Level.WARNING -> w(log)
            Event.Level.ERROR -> e(log)
            Event.Level.FATAL -> e(log)
        }
    }

    fun log(log: String, level: DebugLogLevel = DebugLogLevel.DEBUG) {
        if (BuildConfig.DEBUG) {
            logger.onNext(DebugLogItem(log = log, level = level))
        }
    }
}
