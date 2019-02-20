package com.ringoid.origin.feed.adapter.profile

import android.view.View
import com.bumptech.glide.request.RequestOptions
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideControls
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowControls
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.image.ImageLoader
import kotlinx.android.synthetic.main.rv_item_profile_image.view.*

abstract class BaseProfileImageViewHolder(view: View) : BaseViewHolder<ProfileImageVO>(view)

class ProfileImageViewHolder(view: View, private val isLikeEnabled: Boolean = true) : BaseProfileImageViewHolder(view) {

    init {
        itemView.ibtn_like.changeVisibility(isVisible = isLikeEnabled)
    }

    override fun bind(model: ProfileImageVO) {
        showControls()  // cancel any effect caused by applied payloads
        ImageLoader.load(uri = model.image.uri, imageView = itemView.iv_image,
            options = RequestOptions()
                .override(itemView.width, itemView.height)
                .centerCrop())

        setLiked(isLiked = model.isLiked)
    }

    override fun bind(model: ProfileImageVO, payloads: List<Any>) {
        if (payloads.contains(FeedViewHolderHideControls)) {
            hideControls()
        }
        if (payloads.contains(FeedViewHolderShowControls)) {
            showControls()
        }
    }

    private fun hideControls() {
        itemView.ibtn_like.changeVisibility(isVisible = false)
    }

    private fun showControls() {
        itemView.ibtn_like.changeVisibility(isVisible = isLikeEnabled)
    }

    internal fun setLiked(isLiked: Boolean) {
        itemView.ibtn_like.setImageResource(if (isLiked) R.drawable.ic_like_red else R.drawable.ic_like_outline_white)
    }
}

class HeaderProfileImageViewHolder(view: View) : BaseProfileImageViewHolder(view) {

    override fun bind(model: ProfileImageVO) {
        // no-op
    }
}
