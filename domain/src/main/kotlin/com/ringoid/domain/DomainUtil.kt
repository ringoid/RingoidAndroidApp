package com.ringoid.domain

import com.ringoid.domain.debug.DebugLogUtil

object DomainUtil {

    const val BAD_ID: String = ""
    const val BAD_POSITION: Int = -1
    const val BAD_SORT_POSITION: Int = Int.MAX_VALUE
    const val BAD_RESOURCE: Int = 0
    const val BAD_VALUE = -1
    const val UNKNOWN_VALUE = 0
    const val CURRENT_USER_ID: String = "currentUserId"
    const val DEBOUNCE_NET = 400L
    const val LIMIT_PER_PAGE = 100
    const val LOAD_MORE_THRESHOLD = 10  // items left to end

    const val CLIPBOARD_KEY_CHAT_MESSAGE = "chatMessage"
    const val CLIPBOARD_KEY_CUSTOMER_ID = "customerId"
    const val CLIPBOARD_KEY_DEBUG = "debug"

    const val SOURCE_FEED_EXPLORE = "new_faces"
    const val SOURCE_FEED_LIKES = "who_liked_me"
    const val SOURCE_FEED_MATCHES = "matches"
    const val SOURCE_FEED_MESSAGES = "messages"
    const val SOURCE_FEED_PROFILE = "profile"

    // ------------------------------------------
    private var withError: Boolean = false

    fun withSimulatedError(): Boolean {
        val value = withError
        withError = false
        return value
    }

    fun simulateError() {
        DebugLogUtil.w("Next request will fail with simulated error")
        withError = true
    }
}
