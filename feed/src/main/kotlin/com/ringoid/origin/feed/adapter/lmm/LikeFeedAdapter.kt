package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

class LikeFeedAdapter : BaseLmmAdapter() {

    var onLikeImageListener: ((model: ProfileImageVO, feedItemPosition: Int) -> Unit)? = null

    override fun instantiateViewHolder(view: View): LmmViewHolder =
        LikeFeedViewHolder(view, viewPool = imagesViewPool).also { vh ->
            vh.itemView.ibtn_message.changeVisibility(isVisible = false)  // hide message button initially
            vh.profileImageAdapter.itemClickListener = wrapOnImageClickListenerByFeedItem(vh, onLikeImageListener)
        }
}
