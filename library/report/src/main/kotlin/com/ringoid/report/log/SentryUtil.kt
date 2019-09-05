package com.ringoid.report.log

import io.reactivex.Completable
import io.reactivex.Single

object SentryUtil : ILoggerDelegate {

    private val delegate = SentryLogger()

    override fun breadcrumb(message: String, vararg data: Pair<String, String>) {
        delegate.breadcrumb(message, *data)
    }

    override fun d(message: String, extras: List<Pair<String, String>>?) = delegate.d(message = message, extras = extras)
    override fun i(message: String, extras: List<Pair<String, String>>?) = delegate.i(message = message, extras = extras)
    override fun w(message: String, extras: List<Pair<String, String>>?) = delegate.w(message = message, extras = extras)
    override fun e(message: String, extras: List<Pair<String, String>>?) = delegate.e(message = message, extras = extras)
    override fun a(message: String, extras: List<Pair<String, String>>?) = delegate.a(message = message, extras = extras)

    override fun capture(e: Throwable, message: String?, level: Level, `object`: Any?,
                         tag: String?, extras: List<Pair<String, String>>?) {
        delegate.capture(e = e, message = message, level = level, `object` = `object`, tag = tag, extras = extras)
    }

    // ------------------------------------------
    override fun setUser(currentUserId: String?) {
        delegate.setUser(currentUserId)
    }

    override fun clear() {
        delegate.clear()
    }
}

fun Completable.breadcrumb(message: String, vararg data: Pair<String, String>): Completable =
    doOnSubscribe { SentryUtil.breadcrumb(message, *data) }

inline fun <reified T> Single<T>.breadcrumb(message: String, vararg data: Pair<String, String>): Single<T> =
    doOnSubscribe { SentryUtil.breadcrumb(message, *data) }