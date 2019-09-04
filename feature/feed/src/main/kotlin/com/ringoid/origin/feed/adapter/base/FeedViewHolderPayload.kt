package com.ringoid.origin.feed.adapter.base

import com.ringoid.origin.feed.misc.OffsetScrollStrategy

sealed class FeedViewHolderPayload
sealed class FeedViewHolderAnimatePayload

object FeedViewHolderRebind : FeedViewHolderPayload()
object FeedItemViewHolderAnimateLike : FeedViewHolderAnimatePayload()

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
object FeedViewHolderHideStatusOnScroll : FeedViewHolderPayload()
object FeedViewHolderShowStatusOnScroll : FeedViewHolderPayload()
object FeedViewHolderHideTabsIndicatorOnScroll : FeedViewHolderPayload()
object FeedViewHolderShowTabsIndicatorOnScroll : FeedViewHolderPayload()

object FeedFooterViewHolderHideControls : FeedViewHolderPayload()
object FeedFooterViewHolderShowControls : FeedViewHolderPayload()

data class FeedViewHolderHideOnScroll(val index: Int) : FeedViewHolderPayload()
data class FeedViewHolderShowOnScroll(val index: Int) : FeedViewHolderPayload()
data class FeedViewHolderHideNameOnScroll(val index: Int, val type: OffsetScrollStrategy.Type) : FeedViewHolderPayload()
data class FeedViewHolderShowNameOnScroll(val index: Int, val type: OffsetScrollStrategy.Type) : FeedViewHolderPayload()
