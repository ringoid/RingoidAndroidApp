package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.image.ImageRequest
import kotlinx.android.synthetic.main.rv_item_feed_profile_content.view.*

open class MessagesFeedAdapter(imageLoader: ImageRequest) : BaseLmmAdapter(imageLoader) {

    var onImageToOpenChatClickListener: ((model: ProfileImageVO, feedItemPosition: Int) -> Unit)? = null

    override fun instantiateViewHolder(view: View): LmmViewHolder =
        MessagesFeedViewHolder(view, viewPool = imagesViewPool, imageLoader = imageLoader)
            .also { vh ->
                vh.itemView.ibtn_like.changeVisibility(isVisible = false)
                vh.profileImageAdapter.also { adapter ->
                    adapter.isLikeEnabled = false  // avoid like action by double click on profile image
                    adapter.itemClickListener = wrapOnImageClickListenerByFeedItem(vh, onImageToOpenChatClickListener)
                }
            }
}
