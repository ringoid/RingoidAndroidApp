package com.ringoid.origin.feed.view.lmm.like

import androidx.recyclerview.widget.LinearLayoutManager
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.lmm.LmmViewHolder
import com.ringoid.origin.feed.adapter.lmm.like.BaseLikeFeedAdapter
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedFragment
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedViewModel
import kotlinx.android.synthetic.main.fragment_feed.*

abstract class BaseLikesFeedFragment<VM : BaseLmmFeedViewModel, VH : LmmViewHolder>
    : BaseLmmFeedFragment<VM, VH>() {

    abstract fun instantiateFeedAdapter(): BaseLikeFeedAdapter<VH>

    override fun createFeedAdapter(): BaseLikeFeedAdapter<VH> =
        instantiateFeedAdapter().apply {
            messageClickListener = { model: FeedItem, position: Int ->
                (rv_items.layoutManager as LinearLayoutManager)
                    .scrollToPositionWithOffset(position, 0)
                // TOO: open chat
            }
        }
}
