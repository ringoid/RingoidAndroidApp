package com.ringoid.origin.feed.adapter.explore

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.origin.feed.adapter.base.*
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.rv_item_feed_footer.view.*
import kotlinx.android.synthetic.main.rv_item_feed_profile_content.view.*

class ExploreFeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseFeedViewHolder(view, viewPool) {

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

class HeaderFeedViewHolder(view: View) : OriginFeedViewHolder(view), IFeedViewHolder {

    override fun bind(model: FeedItemVO) {
        // no-op
    }
}

open class FooterFeedViewHolder(view: View) : OriginFeedViewHolder(view), IFeedViewHolder {

    override fun bind(model: FeedItemVO) {
        showControls()
    }

    override fun bind(model: FeedItemVO, payloads: List<Any>) {
        if (payloads.contains(FeedFooterViewHolderHideControls)) {
            hideControls()
        }
        if (payloads.contains(FeedFooterViewHolderShowControls)) {
            showControls()
        }
   }

    // ------------------------------------------
    private fun hideControls() {
        itemView.tv_end_item.changeVisibility(isVisible = false)
    }

    private fun showControls() {
        itemView.tv_end_item.changeVisibility(isVisible = true)
    }
}

// apply same show / hide properties as for footer item
class ErrorFeedViewHolder(view: View) : FooterFeedViewHolder(view)
