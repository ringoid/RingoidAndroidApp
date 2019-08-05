package com.ringoid.domain

import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.debug.DebugOnly

object DomainUtil {

    const val BAD_ID: String = ""
    const val BAD_POSITION: Int = -1
    const val BAD_PROPERTY = "unknown"
    const val BAD_RESOURCE: Int = 0
    const val BAD_SORT_POSITION: Int = Int.MAX_VALUE
    const val BAD_VALUE = -1
    const val UNKNOWN_VALUE = 0
    const val CURRENT_USER_ID: String = "currentUserId"
    const val DEBOUNCE_NET = 400L
    const val DEBOUNCE_PUSH = 300L
    const val LIMIT_PER_PAGE = 40
    const val LIMIT_PER_PAGE_LMM = 5
    const val LOAD_MORE_THRESHOLD = 10  // items left to end

    const val CLIPBOARD_KEY_CHAT_MESSAGE = "chatMessage"
    const val CLIPBOARD_KEY_CUSTOMER_ID = "customerId"
    const val CLIPBOARD_KEY_DEBUG = "debug"

    const val FILTER_MIN_AGE = 18
    const val FILTER_MAX_AGE = 55
    const val FILTER_MIN_DISTANCE = 1000
    const val FILTER_MAX_DISTANCE = 150_000

    const val SOURCE_FEED_EXPLORE = "new_faces"
    const val SOURCE_FEED_LIKES = "who_liked_me"
    const val SOURCE_FEED_MATCHES = "matches"
    const val SOURCE_FEED_MESSAGES = "messages"
    const val SOURCE_FEED_PROFILE = "profile"

    // ------------------------------------------
    @DebugOnly private var withError: Boolean = false
    @DebugOnly private var withThreadInfo: Boolean = BuildConfig.DEBUG

    @DebugOnly
    fun withSimulatedError(): Boolean {
        val value = withError
        withError = false
        return value
    }

    @DebugOnly
    fun withThreadInfo(): Boolean = withThreadInfo

    @DebugOnly
    fun dumpThreadInfo() {
        withThreadInfo = !withThreadInfo
        DebugLogUtil.w(if (withThreadInfo) "Start dumping thread info" else "Stop dumping thread info")
    }

    @DebugOnly
    fun simulateError() {
        DebugLogUtil.w("Next request will fail with simulated error")
        withError = true
    }
}
