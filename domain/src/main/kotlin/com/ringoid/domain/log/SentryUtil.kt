package com.ringoid.domain.log

import android.os.Build
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.exception.ApiException
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.utility.stackTraceString
import io.reactivex.Completable
import io.reactivex.Single
import io.sentry.Sentry
import io.sentry.event.*
import timber.log.Timber
import java.util.*

object SentryUtil {

    class S constructor(private val `object`: Any? = null) {

        fun d(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.DEBUG,   extras = extras)
        fun i(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.INFO,    extras = extras)
        fun w(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.WARNING, extras = extras)
        fun e(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.ERROR,   extras = extras)
        fun a(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.FATAL,   extras = extras)

        fun capture(e: Throwable, message: String? = null, tag: String? = null, extras: List<Pair<String, String>>? = null) {
            capture(e, message = message, `object` = `object`, tag = tag, extras = extras)
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
            DebugLogUtil.v("Breadcrumb: $message")
        } catch (e: Throwable) {
            capture(e, "Failed to record breadcrumb: $message")
        }
    }

    fun d(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.DEBUG, extras = extras)
    fun i(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.INFO, extras = extras)
    fun w(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.WARNING, extras = extras)
    fun e(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.ERROR, extras = extras)
    fun a(message: String, extras: List<Pair<String, String>>? = null) = log(message = message, level = Event.Level.FATAL, extras = extras)

    fun capture(e: Throwable, message: String? = null, level: Event.Level = Event.Level.ERROR,
                tag: String? = null, extras: List<Pair<String, String>>? = null) {
        Timber.d(message)
        val fullExtras = mutableListOf<Pair<String, String>>()
            .apply {
                add(e.javaClass.simpleName to e.stackTraceString())
                extras?.let { addAll(it) }
            }
        capture(e, message = message ?: e.message ?: e.javaClass.simpleName, level = level, `object` = null, tag = tag, extras = fullExtras)
    }

    fun setUser(spm: ISharedPrefsManager) {
        spm.currentUserId()?.let {
            userId = it
            val data = mutableMapOf<String, Any>()//.apply { put("accessToken", spm.accessToken()?.accessToken ?: "null") }
            Sentry.getContext().user = UserBuilder().setId(it).setData(data).build()
        }
    }

    private var userId: String? = null

    fun clear() {
        userId = null
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private fun log(message: String, level: Event.Level, `object`: Any? = null,
                    extras: List<Pair<String, String>>? = null) {
        Sentry.capture(createEvent(message = message, level = level, `object` = `object`, extras = extras))
    }

    private fun capture(e: Throwable, message: String? = null, level: Event.Level = Event.Level.ERROR,
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
        DebugLogUtil.e(e, message.orEmpty(), tag = tag)  // capture exception to debug logs
    }

    private fun createEvent(message: String, level: Event.Level, `object`: Any? = null,
                            extras: List<Pair<String, String>>? = null): Event {
        val user: User? = Sentry.getContext().user
        val builder = EventBuilder()
            .withBreadcrumbs(Sentry.getContext().breadcrumbs)
            .withLevel(level)
            .withMessage(message)
            .withPlatform("Android: ${Build.VERSION.SDK_INT}")
            .withRelease(BuildConfig.VERSION_NAME)
            .withTimestamp(Date())
            .withExtra("userId", user?.id ?: userId)
            .withExtra("appVersion", BuildConfig.BUILD_NUMBER)
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