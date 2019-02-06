package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.base.BaseFeedViewHolder
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideControls
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowControls
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.utility.changeVisibility
import com.ringoid.widget.view.Direction
import kotlinx.android.synthetic.main.rv_item_feed_profile_content.view.*
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

interface ILmmViewHolder

abstract class OriginLmmViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : OriginFeedViewHolder<FeedItem>(view, viewPool)

abstract class BaseLmmViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
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
        fun hideControls() {
            itemView.ibtn_message.changeVisibility(isVisible = false)
        }
        fun showControls() {
            itemView.ibtn_message.changeVisibility(isVisible = true)
        }

        if (payloads.contains(FeedViewHolderHideControls)) {
            hideControls()
        } else {
            showControls()  // TODO: do we need that branch?
        }
        if (payloads.contains(FeedViewHolderShowControls)) {
            showControls()
        }
        super.bind(model, payloads)
    }
}

open class HeaderLmmViewHolder(view: View) : BaseViewHolder<FeedItem>(view), ILmmViewHolder {

    override fun bind(model: FeedItem, payloads: List<Any>) {
        // no-op
    }
}
