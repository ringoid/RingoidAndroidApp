package com.ringoid.origin.view.main

import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.push.PushNotificationData
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
    MESSAGES(DomainUtil.SOURCE_FEED_MESSAGES, 2);

    companion object {
        fun from(sourceFeed: String?): LmmNavTab? =
            when (sourceFeed) {
                DomainUtil.SOURCE_FEED_LIKES -> LmmNavTab.LIKES
                DomainUtil.SOURCE_FEED_MATCHES -> LmmNavTab.MATCHES
                DomainUtil.SOURCE_FEED_MESSAGES -> LmmNavTab.MESSAGES
                else -> null
            }

        fun fromPushType(pushType: String): LmmNavTab? =
            when (pushType) {
                PushNotificationData.TYPE_LIKE -> LmmNavTab.LIKES
                PushNotificationData.TYPE_MATCH -> LmmNavTab.MATCHES
                PushNotificationData.TYPE_MESSAGE -> LmmNavTab.MESSAGES
                else -> null
            }
    }
}
