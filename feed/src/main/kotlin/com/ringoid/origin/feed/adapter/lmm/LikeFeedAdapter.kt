package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.image.ImageRequest

class LikeFeedAdapter(imageLoader: ImageRequest) : BaseLmmAdapter(imageLoader) {

    var onLikeImageListener: ((model: ProfileImageVO, feedItemPosition: Int) -> Unit)? = null

    override fun instantiateViewHolder(view: View): LmmViewHolder =
        LikeFeedViewHolder(view, viewPool = imagesViewPool, imageLoader = imageLoader)
            .also { vh ->
                vh.profileImageAdapter.itemDoubleClickListener = { model, position ->
                    wrapOnImageClickListenerByFeedItem(vh, onLikeImageListener)?.invoke(model, position)
                }
            }
}
