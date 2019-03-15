package com.ringoid.domain.debug

import com.ringoid.domain.BuildConfig
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import io.sentry.event.Event
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.log

@DebugOnly
object DebugLogUtil {

    val logger = ReplaySubject.createWithTimeAndSize<DebugLogItem>(15, TimeUnit.SECONDS, Schedulers.newThread(), 10)
    private var dao: IDebugLogDaoHelper? = null

    fun b(log: String) = log(log, DebugLogLevel.BUS)
    fun v(log: String) = log(log, DebugLogLevel.VERBOSE)
    fun d(log: String) = log(log, DebugLogLevel.DEBUG)
    fun i(log: String) = log(log, DebugLogLevel.INFO)
    fun w(log: String) = log(log, DebugLogLevel.WARNING)
    fun e(log: String) = log(log, DebugLogLevel.ERROR)
    fun e(e: Throwable) = log(log = "${e.message}".trim(), level = DebugLogLevel.ERROR)

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
        Timber.log(level.priority, log)
        if (BuildConfig.IS_STAGING) {
            val logItem = DebugLogItem(log = log, level = level)
            logger.onNext(logItem)
            dao?.addDebugLog(logItem)
        }
    }

    @Synchronized
    fun clear() {
        if (BuildConfig.IS_STAGING) {
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
}
