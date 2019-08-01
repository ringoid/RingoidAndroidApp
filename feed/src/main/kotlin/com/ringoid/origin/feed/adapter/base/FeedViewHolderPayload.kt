package com.ringoid.origin.feed.adapter.base

import com.ringoid.origin.feed.misc.OffsetScrollStrategy

sealed class FeedViewHolderPayload
sealed class FeedViewHolderAnimatePayload

object FeedItemViewHolderAnimateLike : FeedViewHolderAnimatePayload()

object FeedViewHolderHideControls : FeedViewHolderPayload()
object FeedViewHolderShowControls : FeedViewHolderPayload()

object FeedViewHolderHideAboutOnScroll : FeedViewHolderPayload()
object FeedViewHolderShowAboutOnScroll : FeedViewHolderPayload()
object FeedViewHolderHideChatBtnOnScroll : FeedViewHolderPayload()
object FeedViewHolderShowChatBtnOnScroll : FeedViewHolderPayload()
object FeedViewHolderHideLikeBtnOnScroll : FeedViewHolderPayload()
object FeedViewHolderShowLikeBtnOnScroll : FeedViewHolderPayload()
object FeedViewHolderHideOnlineStatusOnScroll : FeedViewHolderPayload()
object FeedViewHolderShowOnlineStatusOnScroll : FeedViewHolderPayload()
object FeedViewHolderHideSettingsBtnOnScroll : FeedViewHolderPayload()
object FeedViewHolderShowSettingsBtnOnScroll : FeedViewHolderPayload()
object FeedViewHolderShowTabsIndicatorOnScroll : FeedViewHolderPayload()
object FeedViewHolderHideTabsIndicatorOnScroll : FeedViewHolderPayload()

data class FeedViewHolderHideOnScroll(val index: Int) : FeedViewHolderPayload()
data class FeedViewHolderShowOnScroll(val index: Int) : FeedViewHolderPayload()
data class FeedViewHolderHideNameOnScroll(val index: Int, val type: OffsetScrollStrategy.Type) : FeedViewHolderPayload()
data class FeedViewHolderShowNameOnScroll(val index: Int, val type: OffsetScrollStrategy.Type) : FeedViewHolderPayload()
