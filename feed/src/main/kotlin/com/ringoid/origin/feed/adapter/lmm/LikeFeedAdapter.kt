package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.image.ImageRequest
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

class LikeFeedAdapter(imageLoader: ImageRequest) : BaseLmmAdapter(imageLoader) {

    override fun instantiateViewHolder(view: View): LmmViewHolder =
        LikesFeedViewHolder(view, viewPool = imagesViewPool, imageLoader = imageLoader)
            .also { vh ->
                vh.itemView.iv_message.changeVisibility(isVisible = false)
                vh.profileImageAdapter.itemDoubleClickListener = { model, position ->
                    wrapOnImageClickListenerByFeedItem(vh, onLikeImageListener)?.invoke(model, position)
                }
            }
}
