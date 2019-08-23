package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

class LikeFeedAdapter : BaseLmmAdapter() {

    override fun instantiateViewHolder(view: View): LmmViewHolder =
        LikesFeedViewHolder(view, viewPool = imagesViewPool)
            .also { vh ->
                vh.itemView.iv_message.changeVisibility(isVisible = false)
                vh.profileImageAdapter.itemDoubleClickListener = { model, position ->
                    wrapOnImageClickListenerByFeedItem(vh, onLikeImageListener)?.invoke(model, position)
                }
            }
}
