package com.ringoid.domain.debug

import io.reactivex.subjects.PublishSubject

object DebugLogUtil {

    val logger: PublishSubject<DebugLogItem> = PublishSubject.create()

    fun v(log: String) = log(log, DebugLogLevel.VERBOSE)
    fun d(log: String) = log(log, DebugLogLevel.DEBUG)
    fun i(log: String) = log(log, DebugLogLevel.INFO)
    fun w(log: String) = log(log, DebugLogLevel.WARNING)
    fun e(log: String) = log(log, DebugLogLevel.ERROR)
    fun e(e: Throwable) = log(log = "${e.javaClass.simpleName}: ${e.message}".trim(), level = DebugLogLevel.ERROR)

    private fun log(log: String, level: DebugLogLevel = DebugLogLevel.DEBUG) {
        logger.onNext(DebugLogItem(log = log, level = level))
    }
}
