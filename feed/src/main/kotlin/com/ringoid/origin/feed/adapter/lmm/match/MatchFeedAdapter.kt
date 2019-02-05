package com.ringoid.origin.feed.adapter.lmm.match

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.adapter.lmm.like.BaseLikeFeedAdapter
import com.ringoid.origin.feed.model.ProfileImageVO
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

class MatchFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool? = null)
    : BaseLikeFeedAdapter<OriginFeedViewHolder<FeedItem>>(imagesViewPool) {

    var onImageToOpenChatClickListener: ((model: ProfileImageVO, position: Int) -> Unit)? = null

    override fun instantiateViewHolder(view: View): OriginFeedViewHolder<FeedItem> =
        MatchFeedViewHolder(view, viewPool = imagesViewPool).also { vh ->
            vh.profileImageAdapter.also { adapter ->
                adapter.isLikeEnabled = false  // hide like button on matches feed items
                adapter.itemClickListener = wrapOnImageClickListener(vh, onImageToOpenChatClickListener)
            }
            (vh.itemView.ibtn_message.layoutParams as? ConstraintLayout.LayoutParams)
                ?.apply { verticalBias = 0.28f }?.let { vh.itemView.ibtn_message.layoutParams = it }
        }

    override fun instantiateHeaderViewHolder(view: View) = HeaderMatchFeedViewHolder(view)
}
