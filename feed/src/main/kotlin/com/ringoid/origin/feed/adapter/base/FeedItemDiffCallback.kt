package com.ringoid.origin.feed.adapter.base

import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.model.FeedItemVO

class FeedItemDiffCallback : BaseDiffCallback<FeedItem>() {

    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        oldItem == newItem  // as 'data class'
}

class FeedItemVODiffCallback : BaseDiffCallback<FeedItemVO>() {

    override fun areItemsTheSame(oldItem: FeedItemVO, newItem: FeedItemVO): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: FeedItemVO, newItem: FeedItemVO): Boolean =
        oldItem === newItem  // as 'data class'
}
