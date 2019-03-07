package com.ringoid.domain

object DomainUtil {

    const val BAD_ID: String = ""
    const val BAD_POSITION: Int = -1
    const val BAD_SORT_POSITION: Int = Int.MAX_VALUE
    const val BAD_RESOURCE: Int = 0
    const val CURRENT_USER_ID: String = "currentUserId"
    const val DEBOUNCE_NET = 400L
    const val LIMIT_PER_PAGE = 20
    const val LOAD_MORE_THRESHOLD = 5  // items left to end

    const val CLIPBOARD_KEY_CHAT_MESSAGE = "chatMessage"
    const val CLIPBOARD_KEY_CUSTOMER_ID = "customerId"
    const val CLIPBOARD_KEY_DEBUG = "debug"

    const val SOURCE_FEED_LIKES = "lmm_likes"
    const val SOURCE_FEED_MATCHES = "lmm_matches"
    const val SOURCE_FEED_MESSAGES = "lmm_messages"
}
