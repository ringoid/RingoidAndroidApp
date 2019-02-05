package com.ringoid.domain

import android.os.Build
import com.ringoid.utility.stackTraceString
import io.reactivex.Completable
import io.reactivex.Single
import io.sentry.Sentry
import io.sentry.event.Breadcrumb
import io.sentry.event.BreadcrumbBuilder
import io.sentry.event.Event
import io.sentry.event.EventBuilder
import java.util.*

object SentryUtil {

    class S constructor(private val `object`: Any? = null) {

        fun d(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.DEBUG,   extras = extras)
        fun i(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.INFO,    extras = extras)
        fun w(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.WARNING, extras = extras)
        fun e(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.ERROR,   extras = extras)
        fun a(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.FATAL,   extras = extras)

        fun capture(e: Throwable, message: String? = null, extras: List<Pair<String, String>>? = null) {
            capture(e, message = message, `object` = `object`, extras = extras)
        }

        private fun log(message: String, level: Event.Level, extras: List<Pair<String, String>>? = null) {
            log(message, level = level, `object` = `object`, extras = extras)
        }
    }

    fun breadcrumb(message: String, vararg data: Pair<String, String>) {
        try {
            val breadcrumb = BreadcrumbBuilder()
                .setData(data.toMap())
                .setMessage(message)
                .setTimestamp(Date())
                .setType(Breadcrumb.Type.DEFAULT)
                .build()
            Sentry.getContext().recordBreadcrumb(breadcrumb)
        } catch (e: Throwable) {
            capture(e, "Failed to record breadcrumb: $message")
        }
    }

    fun d(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.DEBUG,   extras = extras)
    fun i(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.INFO,    extras = extras)
    fun w(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.WARNING, extras = extras)
    fun e(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.ERROR,   extras = extras)
    fun a(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.FATAL,   extras = extras)

    fun capture(e: Throwable, message: String? = null, extras: List<Pair<String, String>>? = null) {
        val fullExtras = mutableListOf<Pair<String, String>>()
            .apply {
                add(e.javaClass.simpleName to e.stackTraceString())
                extras?.let { addAll(it) }
            }
        capture(e, message = message, `object` = null, extras = fullExtras)
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private fun log(message: String, level: Event.Level, `object`: Any? = null,
                    extras: List<Pair<String, String>>? = null) {
        Sentry.capture(createEvent(message = message, level = level, `object` = `object`, extras = extras))
    }

    private fun capture(e: Throwable, message: String? = null, `object`: Any? = null,
                        extras: List<Pair<String, String>>? = null) {
        if (!message.isNullOrBlank()) {
            Sentry.capture(createEvent(message = message, level = Event.Level.ERROR, `object` = `object`, extras = extras))
        } else {
            Sentry.capture(e)
        }
    }

    private fun createEvent(message: String, level: Event.Level, `object`: Any? = null,
                            extras: List<Pair<String, String>>? = null): Event {
        val builder = EventBuilder()
            .withBreadcrumbs(Sentry.getContext().breadcrumbs)
            .withLevel(level)
            .withMessage(message)
            .withPlatform("Android: ${Build.VERSION.SDK_INT}")
            .withRelease(BuildConfig.VERSION_NAME)
            .withTimestamp(Date())
        if (`object` != null) {
            builder.withTag(`object`.javaClass.simpleName, `object`.hashCode().toString())
        }
        extras?.forEach { builder.withExtra(it.first, it.second) }
        return builder.build()
    }
}

fun Completable.breadcrumb(message: String, vararg data: Pair<String, String>): Completable =
    doOnSubscribe { SentryUtil.breadcrumb(message, *data) }

inline fun <reified T> Single<T>.breadcrumb(message: String, vararg data: Pair<String, String>): Single<T> =
    doOnSubscribe { SentryUtil.breadcrumb(message, *data) }