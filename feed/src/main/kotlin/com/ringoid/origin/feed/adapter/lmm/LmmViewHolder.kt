package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.base.BaseFeedViewHolder
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideControls
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowControls
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.utility.changeVisibility
import com.ringoid.widget.view.Direction
import kotlinx.android.synthetic.main.rv_item_feed_profile_content.view.*
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

class LmmViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseFeedViewHolder<FeedItem>(view, viewPool) {

    init {
        itemView.ibtn_message.setOnFlingListener {
            val rv = itemView.rv_items
            when (it) {
                Direction.left -> {  // next page
                    rv.smoothScrollToPosition(adapterPosition + 1)
                }
                Direction.right -> {  // previous page
                    rv.smoothScrollToPosition(adapterPosition - 1)
                }
            }
        }
    }

    override fun bind(model: FeedItem, payloads: List<Any>) {
        if (payloads.contains(FeedViewHolderHideControls)) {
            hideControls()
        }
        if (payloads.contains(FeedViewHolderShowControls)) {
            showControls()
        }
        super.bind(model, payloads)
    }

    // ------------------------------------------------------------------------
    override fun hideControls() {
        super.hideControls()
        itemView.ibtn_message.changeVisibility(isVisible = false)
    }

    override fun showControls() {
        super.showControls()
        itemView.ibtn_message.changeVisibility(isVisible = true)
    }
}

class HeaderLmmViewHolder(view: View) : OriginFeedViewHolder<FeedItem>(view) {

    override fun bind(model: FeedItem) {
        // no-op
    }
}
