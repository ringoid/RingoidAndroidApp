package com.ringoid.origin.feed.adapter.base

sealed class FeedViewHolderPayload

object FeedViewHolderHideControlls : FeedViewHolderPayload()
object FeedViewHolderShowControlls : FeedViewHolderPayload()
