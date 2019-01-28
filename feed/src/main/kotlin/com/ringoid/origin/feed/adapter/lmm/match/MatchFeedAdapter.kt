package com.ringoid.origin.feed.adapter.lmm.match

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.adapter.lmm.like.BaseLikeFeedAdapter
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

class MatchFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool? = null)
    : BaseLikeFeedAdapter<OriginFeedViewHolder<FeedItem>>(imagesViewPool, headerRows = 1) {

    override fun instantiateViewHolder(view: View): OriginFeedViewHolder<FeedItem> =
        MatchFeedViewHolder(view).also { vh ->
            vh.profileImageAdapter.isLikeButtonVisible = false
            (vh.itemView.ibtn_message.layoutParams as? ConstraintLayout.LayoutParams)
                ?.apply { verticalBias = 0.28f }?.let { vh.itemView.ibtn_message.layoutParams = it }
        }

    override fun instantiateHeaderViewHolder(view: View) = HeaderMatchFeedViewHolder(view)
}
