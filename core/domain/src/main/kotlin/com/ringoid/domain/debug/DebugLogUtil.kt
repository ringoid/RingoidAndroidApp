package com.ringoid.domain.debug

import com.ringoid.config.IRuntimeConfig
import com.ringoid.utility.DebugOnly
import com.ringoid.utility.tagLine
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import io.sentry.event.Event
import timber.log.Timber
import java.util.concurrent.TimeUnit

@DebugOnly
object DebugLogUtil {

    val logger = ReplaySubject.createWithTimeAndSize<DebugLogItem>(15, TimeUnit.SECONDS, Schedulers.newThread(), 10)
    private lateinit var config: IRuntimeConfig
    private var dao: IDebugLogDaoHelper? = null

    fun setConfig(config: IRuntimeConfig) {
        this.config = config
    }

    fun lifecycle(`object`: Any, log: String) = log("${`object`.javaClass.simpleName}: $log", DebugLogLevel.LIFECYCLE)
    fun b(log: String) = log(log, DebugLogLevel.BUS)
    fun v(log: String) = log(log, DebugLogLevel.VERBOSE)
    fun d(log: String) = log(log, DebugLogLevel.DEBUG)
    fun i(log: String) = log(log, DebugLogLevel.INFO)
    fun w(log: String) = log(log, DebugLogLevel.WARNING)
    fun e(log: String) = log(log, DebugLogLevel.ERROR)
    fun e(e: Throwable) = e(e, "", null)
    fun e(e: Throwable, message: String, tag: String? = null) =
        log(log = "${e.javaClass.simpleName}[$tag]: $message ${e.message.orEmpty()}".trim(), level = DebugLogLevel.ERROR)

    fun log(log: String, level: Event.Level) {
        when (level) {
            Event.Level.DEBUG -> d(log)
            Event.Level.INFO -> i(log)
            Event.Level.WARNING -> w(log)
            Event.Level.ERROR -> e(log)
            Event.Level.FATAL -> e(log)
        }
    }

    @Synchronized
    fun log(log: String, level: DebugLogLevel = DebugLogLevel.DEBUG) {
        tagLine(prefix = " {Debug Log} ")
        Timber.log(level.priority, log)
        if (config.isDeveloper() && config.collectDebugLogs()) {
            val logItem = DebugLogItem(log = log, level = level)
            logger.onNext(logItem)
            dao?.addDebugLog(logItem)
        }
    }

    @Synchronized
    fun clear() {
        if (config.isDeveloper()) {
            logger.onNext(EmptyDebugLogItem)
            logger.cleanupBuffer()
            dao?.deleteDebugLog()
        }
    }

    // ------------------------------------------
    fun connectToDb(dao: IDebugLogDaoHelper) {
        this.dao = dao
    }

    fun getDebugLog(): Single<List<DebugLogItem>>? =
        dao?.debugLog()
           ?.subscribeOn(Schedulers.io())
           ?.observeOn(AndroidSchedulers.mainThread())

    fun extractLog(): String = dao?.extractLog() ?: "NO DEBUG LOG (cache was not connected)"
}
