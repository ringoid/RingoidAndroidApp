package com.ringoid.origin.feed.adapter.lmm

import android.view.ViewGroup
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.base.BaseFeedViewHolder
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.model.ProfileImageVO

class LikeFeedAdapter : BaseLmmAdapter() {

    var onLikeImageListener: ((model: ProfileImageVO, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OriginFeedViewHolder<FeedItem> {
        val viewHolder = super.onCreateViewHolder(parent, viewType)
        return viewHolder  // perform additional initialization only for VIEW_TYPE_NORMAL view holders
            .takeIf { viewType == VIEW_TYPE_NORMAL }
            ?.let { it as? BaseFeedViewHolder<FeedItem> }
            ?.also { vh ->
                vh.profileImageAdapter.itemClickListener = onLikeImageListener
            } ?: viewHolder  // don't apply additional initializations on non-VIEW_TYPE_NORMAL view holders
    }
}
// TODO: show message button only after like tap
