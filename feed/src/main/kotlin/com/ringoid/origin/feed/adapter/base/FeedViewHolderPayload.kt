package com.ringoid.origin.feed.adapter.base

sealed class FeedViewHolderPayload

object FeedViewHolderHideControls : FeedViewHolderPayload()
object FeedViewHolderShowControls : FeedViewHolderPayload()
object FeedFooterViewHolderHideControls : FeedViewHolderPayload()
object FeedFooterViewHolderShowControls : FeedViewHolderPayload()

object FeedViewHolderHideChatBtnOnScroll : FeedViewHolderPayload()
object FeedViewHolderShowChatBtnOnScroll : FeedViewHolderPayload()
object FeedViewHolderHideSettingsBtnOnScroll : FeedViewHolderPayload()
object FeedViewHolderShowSettingsBtnOnScroll : FeedViewHolderPayload()
object FeedViewHolderShowTabs2IndicatorOnScroll : FeedViewHolderPayload()
object FeedViewHolderHideTabs2IndicatorOnScroll : FeedViewHolderPayload()
