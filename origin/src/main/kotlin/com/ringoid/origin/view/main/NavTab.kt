package com.ringoid.origin.view.main

import com.ringoid.domain.DomainUtil
import com.ringoid.origin.navigation.NavigateFrom

enum class NavTab(val tabName: String) {
    EXPLORE(NavigateFrom.MAIN_TAB_EXPLORE),
    LMM(NavigateFrom.MAIN_TAB_LMM),
    PROFILE(NavigateFrom.MAIN_TAB_PROFILE);  // order matters for BottomBar

    companion object {
        val values: Array<NavTab> = NavTab.values()

        fun get(index: Int): NavTab = values[index]  // here order matters

        fun from(tabName: String): NavTab =
            when (tabName) {
                NavigateFrom.MAIN_TAB_EXPLORE -> NavTab.EXPLORE
                NavigateFrom.MAIN_TAB_LMM -> NavTab.LMM
                NavigateFrom.MAIN_TAB_PROFILE -> NavTab.PROFILE
                else -> throw IllegalArgumentException("Unknown tab name: $tabName")
            }
    }
}

enum class LmmNavTab(val feedName: String, val page: Int) {
    LIKES(DomainUtil.SOURCE_FEED_LIKES, 0),
    MATCHES(DomainUtil.SOURCE_FEED_MATCHES, 1),
    MESSAGES(DomainUtil.SOURCE_FEED_MESSAGES, 2)
}
