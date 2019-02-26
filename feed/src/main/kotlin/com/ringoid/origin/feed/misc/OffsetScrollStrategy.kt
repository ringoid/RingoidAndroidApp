package com.ringoid.origin.feed.misc

import com.ringoid.origin.feed.adapter.base.FeedViewHolderPayload

data class OffsetScrollStrategy(val type: Type, val deltaOffset: Int, val hide: FeedViewHolderPayload, val show: FeedViewHolderPayload) {

    enum class Type { TOP, BOTTOM }
}
