package com.ringoid.origin.feed.adapter.base

sealed class FeedViewHolderPayload

object FeedViewHolderHideControls : FeedViewHolderPayload()
object FeedViewHolderShowControls : FeedViewHolderPayload()
object FeedFooterViewHolderHideControls : FeedViewHolderPayload()
object FeedFooterViewHolderShowControls : FeedViewHolderPayload()

object FeedViewHolderHideChatBtnOnScroll : FeedViewHolderPayload()
object FeedViewHolderShowChatBtnOnScroll : FeedViewHolderPayload()
object FeedViewHolderHideOnlineStatusOnScroll : FeedViewHolderPayload()
object FeedViewHolderShowOnlineStatusOnScroll : FeedViewHolderPayload()
object FeedViewHolderHideSettingsBtnOnScroll : FeedViewHolderPayload()
object FeedViewHolderShowSettingsBtnOnScroll : FeedViewHolderPayload()
object FeedViewHolderShowTabsIndicatorOnScroll : FeedViewHolderPayload()
object FeedViewHolderHideTabsIndicatorOnScroll : FeedViewHolderPayload()

data class FeedViewHolderHideOnScroll(val index: Int) : FeedViewHolderPayload()
data class FeedViewHolderShowOnScroll(val index: Int) : FeedViewHolderPayload()
