package com.ringoid.origin.view.main

import com.ringoid.domain.DomainUtil

enum class NavTab {
    EXPLORE, LMM, PROFILE;  // order matters

    companion object {
        val values: Array<NavTab> = NavTab.values()

        fun get(index: Int): NavTab = values[index]
    }
}

enum class LmmNavTab(val feedName: String, val page: Int) {
    LIKES(DomainUtil.SOURCE_FEED_LIKES, 0),
    MATCHES(DomainUtil.SOURCE_FEED_MATCHES, 1),
    MESSAGES(DomainUtil.SOURCE_FEED_MESSAGES, 2)
}
