package com.ringoid.origin.feed.adapter.base

sealed class FeedViewHolderPayload

object FeedViewHolderHideControls : FeedViewHolderPayload()
object FeedViewHolderShowControls : FeedViewHolderPayload()

object LikeFeedViewHolderHideChatControls : FeedViewHolderPayload()
object LikeFeedViewHolderShowChatControls : FeedViewHolderPayload()
