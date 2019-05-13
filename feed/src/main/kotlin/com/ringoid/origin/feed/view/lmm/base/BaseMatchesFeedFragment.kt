package com.ringoid.origin.feed.view.lmm.base

import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.adapter.base.*
import com.ringoid.origin.feed.misc.OffsetScrollStrategy

abstract class BaseMatchesFeedFragment<VM : BaseLmmFeedViewModel> : BaseLmmFeedFragment<VM>() {

    /* Scroll listeners */
    // --------------------------------------------------------------------------------------------
    override fun getOffsetScrollStrategies(): List<OffsetScrollStrategy> =
        mutableListOf<OffsetScrollStrategy>()
            .apply {
                addAll(super.getOffsetScrollStrategies())
                add(OffsetScrollStrategy(type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_BIAS_BTN_BOTTOM, hide = FeedViewHolderHideChatBtnOnScroll, show = FeedViewHolderShowChatBtnOnScroll))
                add(OffsetScrollStrategy(type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_TABS_INDICATOR_TOP, hide = FeedViewHolderHideTabsIndicatorOnScroll, show = FeedViewHolderShowTabsIndicatorOnScroll))
                add(OffsetScrollStrategy(type = OffsetScrollStrategy.Type.UP, deltaOffset = AppRes.FEED_ITEM_TABS_INDICATOR_TOP2, hide = FeedViewHolderHideTabs2IndicatorOnScroll, show = FeedViewHolderShowTabs2IndicatorOnScroll))
                add(OffsetScrollStrategy(type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_SETTINGS_BTN_TOP, hide = FeedViewHolderHideSettingsBtnOnScroll, show = FeedViewHolderShowSettingsBtnOnScroll))
                add(OffsetScrollStrategy(type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_BIAS_BTN_TOP, hide = FeedViewHolderHideChatBtnOnScroll, show = FeedViewHolderShowChatBtnOnScroll))
            }
}
