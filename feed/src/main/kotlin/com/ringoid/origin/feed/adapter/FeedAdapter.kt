package com.ringoid.origin.feed.adapter

import android.view.View
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.*
import com.ringoid.origin.feed.model.EmptyFeedItemVO
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.image.ImageRequest

class FeedAdapter(imageLoader: ImageRequest) : BaseFeedAdapter(imageLoader, FeedItemVODiffCallback()) {

    var onLikeImageListener: ((model: ProfileImageVO, position: Int) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.rv_item_feed_profile

    override fun instantiateViewHolder(view: View): OriginFeedViewHolder =
        FeedViewHolder(view, viewPool = imagesViewPool, imageLoader = imageLoader)
            .also { vh ->
                vh.profileImageAdapter.itemClickListener = { model, position ->
                    getModel(vh.adapterPosition).likedImages[model.image.id] = model.isLiked
                    onLikeImageListener?.invoke(model, position)
                }
            }

    override fun instantiateHeaderViewHolder(view: View) = HeaderFeedViewHolder(view)
    override fun instantiateFooterViewHolder(view: View) = FooterFeedViewHolder(view)

    // ------------------------------------------
    override fun getStubItem(): FeedItemVO = EmptyFeedItemVO

    override fun getFooterLayoutResId(): Int = R.layout.rv_item_feed_footer
}
