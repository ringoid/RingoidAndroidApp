package com.ringoid.origin.feed.adapter.base

import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.domain.model.feed.FeedItem

class FeedItemDiffCallback : BaseDiffCallback<FeedItem>() {

    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        oldItem.sameContent(newItem)
}
