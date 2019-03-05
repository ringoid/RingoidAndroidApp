package com.ringoid.origin.feed.adapter.lmm

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.ringoid.origin.feed.adapter.base.BaseFeedViewHolder
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.model.ProfileImageVO
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

open class MatchFeedAdapter : BaseLmmAdapter() {

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
                (vh.itemView.ibtn_message.layoutParams as? ConstraintLayout.LayoutParams)
                    ?.apply { verticalBias = 0.28f }?.let { vh.itemView.ibtn_message.layoutParams = it }
            } ?: viewHolder  // don't apply additional initializations on non-VIEW_TYPE_NORMAL view holders
    }
}
