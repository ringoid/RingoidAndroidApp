package com.ringoid.report.log

import android.os.Build
import com.ringoid.report.exception.ApiException
import com.ringoid.utility.BuildConfig
import com.ringoid.utility.stackTraceStringN
import io.sentry.Sentry
import io.sentry.event.*
import timber.log.Timber
import java.util.*

class SentryLogger : ILoggerDelegate {

    companion object {
        private const val MAX_BREADCRUMB_LENGTH = 400
    }

    override fun breadcrumb(message: String, vararg data: Pair<String, String>) {
        Timber.v("Breadcrumb: $message")
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

    override fun d(message: String, extras: List<Pair<String, String>>?) = log(message = message, level = ReportLevel.DEBUG, extras = extras)
    override fun i(message: String, extras: List<Pair<String, String>>?) = log(message = message, level = ReportLevel.INFO, extras = extras)
    override fun w(message: String, extras: List<Pair<String, String>>?) = log(message = message, level = ReportLevel.WARNING, extras = extras)
    override fun e(message: String, extras: List<Pair<String, String>>?) = log(message = message, level = ReportLevel.ERROR, extras = extras)
    override fun a(message: String, extras: List<Pair<String, String>>?) = log(message = message, level = ReportLevel.FATAL, extras = extras)

    override fun capture(e: Throwable, message: String?, level: ReportLevel,
                         `object`: Any?, tag: String?, extras: List<Pair<String, String>>?) {
        Timber.log(level.toLogPriority(), e, message)
        val fullExtras = mutableListOf<Pair<String, String>>()
            .apply {
                extras?.let { addAll(it) }
                add(e.javaClass.simpleName to e.stackTraceStringN(MAX_BREADCRUMB_LENGTH))
            }
        captureImpl(e, message = message ?: e.message ?: e.javaClass.simpleName, level = level,
                    `object` = `object`, tag = tag, extras = fullExtras)
    }

    // ------------------------------------------
    private var userId: String? = null

    override fun setUser(currentUserId: String?) {
        currentUserId?.let {
            userId = it
            val data = mutableMapOf<String, Any>()//.apply { put("accessToken", spm.accessToken()?.accessToken ?: "null") }
            Sentry.getContext().user = UserBuilder().setId(it).setData(data).build()
        }
    }

    override fun clear() {
        userId = null
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private fun log(message: String, level: ReportLevel, `object`: Any? = null,
                    extras: List<Pair<String, String>>? = null) {
        Timber.log(level.toLogPriority(), message)
        Sentry.capture(createEvent(message = message, level = level, `object` = `object`, extras = extras))
    }

    private fun captureImpl(e: Throwable, message: String? = null, level: ReportLevel = ReportLevel.ERROR,
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

    private fun createEvent(message: String, level: ReportLevel, `object`: Any? = null,
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
