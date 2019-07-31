package com.ringoid.origin.feed.adapter.lmm

import android.view.ViewGroup
import com.ringoid.origin.feed.adapter.base.BaseFeedViewHolder
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.image.ImageRequest
import kotlinx.android.synthetic.main.rv_item_feed_profile_content.view.*

open class MatchFeedAdapter(imageLoader: ImageRequest) : BaseLmmAdapter(imageLoader) {

    var onImageToOpenChatClickListener: ((model: ProfileImageVO, feedItemPosition: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OriginFeedViewHolder {
        val viewHolder = super.onCreateViewHolder(parent, viewType)
        return viewHolder  // perform additional initialization only for VIEW_TYPE_NORMAL view holders
            .takeIf { viewType == VIEW_TYPE_NORMAL }
            ?.let { it as? BaseFeedViewHolder }
            ?.also { vh ->
                vh.itemView.ibtn_like.changeVisibility(isVisible = false)
                vh.profileImageAdapter.also { adapter ->
                    adapter.isLikeEnabled = false  // avoid like action by double click on profile image
                    adapter.itemClickListener = wrapOnImageClickListenerByFeedItem(vh, onImageToOpenChatClickListener)
                }
            } ?: viewHolder  // don't apply additional initializations on non-VIEW_TYPE_NORMAL view holders
    }
}
