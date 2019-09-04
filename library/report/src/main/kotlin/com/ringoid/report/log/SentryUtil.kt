package com.ringoid.report.log

import android.os.Build
import android.util.Log
import com.ringoid.report.exception.ApiException
import com.ringoid.utility.BuildConfig
import com.ringoid.utility.stackTraceStringN
import io.reactivex.Completable
import io.reactivex.Single
import io.sentry.Sentry
import io.sentry.event.*
import timber.log.Timber
import java.util.*

object SentryUtil {

    private const val MAX_BREADCRUMB_LENGTH = 400

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

    class S constructor(private val `object`: Any? = null) {

        fun d(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Level.DEBUG,   extras = extras)
        fun i(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Level.INFO,    extras = extras)
        fun w(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Level.WARNING, extras = extras)
        fun e(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Level.ERROR,   extras = extras)
        fun a(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Level.FATAL,   extras = extras)

        fun capture(e: Throwable, message: String? = null, tag: String? = null, extras: List<Pair<String, String>>? = null) {
            capture(e, message = message, `object` = `object`, tag = tag, extras = extras)
        }

        private fun log(message: String, level: Level, extras: List<Pair<String, String>>? = null) {
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

    fun d(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Level.DEBUG, extras = extras)
    fun i(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Level.INFO, extras = extras)
    fun w(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Level.WARNING, extras = extras)
    fun e(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Level.ERROR, extras = extras)
    fun a(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Level.FATAL, extras = extras)

    fun capture(e: Throwable, message: String? = null, level: Level = Level.ERROR,
                tag: String? = null, extras: List<Pair<String, String>>? = null) {
        Timber.log(level.toLogPriority(), e, message)
        val fullExtras = mutableListOf<Pair<String, String>>()
            .apply {
                extras?.let { addAll(it) }
                add(e.javaClass.simpleName to e.stackTraceStringN(MAX_BREADCRUMB_LENGTH))
            }
        capture(e, message = message ?: e.message ?: e.javaClass.simpleName, level = level, `object` = null, tag = tag, extras = fullExtras)
    }

    // ------------------------------------------
    private var userId: String? = null

    fun setUser(currentUserId: String?) {
        currentUserId?.let {
            userId = it
            val data = mutableMapOf<String, Any>()//.apply { put("accessToken", spm.accessToken()?.accessToken ?: "null") }
            Sentry.getContext().user = UserBuilder().setId(it).setData(data).build()
        }
    }

    fun clear() {
        userId = null
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private fun log(message: String, level: Level, `object`: Any? = null,
                    extras: List<Pair<String, String>>? = null) {
        Timber.log(level.toLogPriority(), message)
        Sentry.capture(createEvent(message = message, level = level, `object` = `object`, extras = extras))
    }

    private fun capture(e: Throwable, message: String? = null, level: Level = Level.ERROR,
                        `object`: Any? = null, tag: String? = null, extras: List<Pair<String, String>>? = null) {
        breadcrumb("Captured exception", "exception" to e.javaClass.canonicalName, "message" to "$message",
                   "exception message" to "${e.message}", "tag" to "$tag", "extras" to "${extras?.joinToString { "[${it.first}:${it.second}]" }}")
        message?.let { breadcrumb(it) }
        if (!message.isNullOrBlank()) {
            val xExtras = (e as? ApiException)?.code
                ?.let { errorCode -> "apiErrorCode" to errorCode }
                ?.let { mutableListOf<Pair<String, String>>().apply { add(it) } }
                ?.let { list -> extras?.let { list.addAll(it) }; list }
                ?: extras
            Sentry.capture(createEvent(message = message, level = level, `object` = `object`, extras = xExtras))
        } else {
            Sentry.capture(e)
        }
    }

    private fun createEvent(message: String, level: Level, `object`: Any? = null,
                            extras: List<Pair<String, String>>? = null): Event {
        val user: User? = Sentry.getContext().user
        val builder = EventBuilder()
            .withBreadcrumbs(Sentry.getContext().breadcrumbs)
            .withLevel(level.lvl)
            .withMessage(message)
            .withPlatform("Android: ${Build.VERSION.SDK_INT}")
            .withRelease(BuildConfig.VERSION_NAME)
            .withTimestamp(Date())
            .withExtra("userId", user?.id ?: userId)
            .withExtra("appVersion", BuildConfig.VERSION_CODE)
        if (`object` != null) {
            builder.withTag(`object`.javaClass.simpleName, `object`.hashCode().toString())
        }
        user?.data?.forEach { builder.withExtra(it.key, it.value) }
        extras?.forEach { builder.withExtra(it.first, it.second) }
        return builder.build()
    }
}

fun Completable.breadcrumb(message: String, vararg data: Pair<String, String>): Completable =
    doOnSubscribe { SentryUtil.breadcrumb(message, *data) }

inline fun <reified T> Single<T>.breadcrumb(message: String, vararg data: Pair<String, String>): Single<T> =
    doOnSubscribe { SentryUtil.breadcrumb(message, *data) }