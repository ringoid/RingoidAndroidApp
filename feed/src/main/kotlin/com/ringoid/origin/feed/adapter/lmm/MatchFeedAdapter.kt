package com.ringoid.origin.feed.adapter.lmm

import android.view.ViewGroup
import com.ringoid.origin.feed.adapter.base.BaseFeedViewHolder
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.image.ImageRequest

open class MatchFeedAdapter(imageLoader: ImageRequest) : BaseLmmAdapter(imageLoader) {

    var onImageToOpenChatClickListener: ((model: ProfileImageVO, feedItemPosition: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OriginFeedViewHolder {
        val viewHolder = super.onCreateViewHolder(parent, viewType)
        return viewHolder  // perform additional initialization only for VIEW_TYPE_NORMAL view holders
            .takeIf { viewType == VIEW_TYPE_NORMAL }
            ?.let { it as? BaseFeedViewHolder }
            ?.also { vh ->
                vh.profileImageAdapter.also { adapter ->
                    adapter.isLikeEnabled = false  // hide like button on matches feed items
                    adapter.itemClickListener = wrapOnImageClickListenerByFeedItem(vh, onImageToOpenChatClickListener)
                }
            } ?: viewHolder  // don't apply additional initializations on non-VIEW_TYPE_NORMAL view holders
    }
}
