package com.ringoid.origin.feed.adapter.base

import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.origin.feed.model.FeedItemVO

class FeedItemVODiffCallback : BaseDiffCallback<FeedItemVO>() {

    override fun areItemsTheSame(oldItem: FeedItemVO, newItem: FeedItemVO): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: FeedItemVO, newItem: FeedItemVO): Boolean =
        oldItem.sameContent(newItem)
}
