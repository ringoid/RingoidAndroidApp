package com.ringoid.origin.view.main

import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.push.PushNotificationData
import com.ringoid.origin.navigation.NavigateFrom

enum class NavTab(val tabName: String) {
    EXPLORE(NavigateFrom.MAIN_TAB_EXPLORE),
    LMM(NavigateFrom.MAIN_TAB_LMM),
    PROFILE(NavigateFrom.MAIN_TAB_PROFILE);  // order matters for BottomBar

    companion object {
        val values: Array<NavTab> = values()

        fun get(index: Int): NavTab = values[index]  // here order matters

        fun from(tabName: String): NavTab =
            when (tabName) {
                NavigateFrom.MAIN_TAB_EXPLORE -> EXPLORE
                NavigateFrom.MAIN_TAB_LMM -> LMM
                NavigateFrom.MAIN_TAB_PROFILE -> PROFILE
                else -> throw IllegalArgumentException("Unknown tab name: $tabName")
            }
    }
}

enum class LmmNavTab(val feedName: String) {
    LIKES(DomainUtil.SOURCE_FEED_LIKES),
    MATCHES(DomainUtil.SOURCE_FEED_MATCHES),
    MESSAGES(DomainUtil.SOURCE_FEED_MESSAGES);  // order matters

    fun page(): Int = ordinal

    companion object {
        private val values: Array<LmmNavTab> = values()

        fun get(index: Int): LmmNavTab? = values[index]  // here order matters

        fun from(sourceFeed: String?): LmmNavTab? =
            when (sourceFeed) {
                DomainUtil.SOURCE_FEED_LIKES -> LIKES
                DomainUtil.SOURCE_FEED_MATCHES -> MATCHES
                DomainUtil.SOURCE_FEED_MESSAGES -> MESSAGES
                else -> null
            }

        fun fromPushType(pushType: String): LmmNavTab? =
            when (pushType) {
                PushNotificationData.TYPE_LIKE -> LIKES
                PushNotificationData.TYPE_MATCH -> MATCHES
                PushNotificationData.TYPE_MESSAGE -> MESSAGES
                else -> null
            }
    }
}
