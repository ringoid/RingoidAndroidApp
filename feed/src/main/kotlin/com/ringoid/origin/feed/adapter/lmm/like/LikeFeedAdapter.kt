package com.ringoid.origin.feed.adapter.lmm.like

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder

class LikeFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool? = null)
    : BaseLikeFeedAdapter<OriginFeedViewHolder<FeedItem>>(imagesViewPool, headerRows = 1) {

    override fun instantiateViewHolder(view: View): OriginFeedViewHolder<FeedItem> = LikeFeedViewHolder(view)

    override fun instantiateHeaderViewHolder(view: View) = HeaderLikeFeedViewHolder(view)
}
