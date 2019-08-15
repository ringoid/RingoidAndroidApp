package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideLikeBtnOnScroll
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowLikeBtnOnScroll
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.rv_item_feed_profile_content.view.*

class LikesFeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : LmmViewHolder(view, viewPool) {

    override fun bind(model: FeedItemVO, payloads: List<Any>) {
        super.bind(model, payloads)

        // scroll affected
        if (payloads.contains(FeedViewHolderHideLikeBtnOnScroll)) {
            itemView.ibtn_like.changeVisibility(isVisible = false)
        }
        if (payloads.contains(FeedViewHolderShowLikeBtnOnScroll)) {
            itemView.ibtn_like.changeVisibility(isVisible = true)
        }
    }

    // ------------------------------------------------------------------------
    override fun hideControls() {
        super.hideControls()
        itemView.ibtn_like.changeVisibility(isVisible = false)
    }

    override fun showControls() {
        super.showControls()
        itemView.ibtn_like.changeVisibility(isVisible = true)
    }
}
