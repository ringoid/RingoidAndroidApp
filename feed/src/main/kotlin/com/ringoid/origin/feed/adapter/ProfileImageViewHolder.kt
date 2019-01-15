package com.ringoid.origin.feed.adapter

import android.view.View
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.ScaleAnimation
import com.bumptech.glide.request.RequestOptions
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.model.image.Image
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.anim.LikeAnimation
import com.ringoid.utility.ImageLoader
import kotlinx.android.synthetic.main.rv_item_profile_image.view.*

class ProfileImageViewHolder(view: View) : BaseViewHolder<Image>(view) {

    override fun bind(model: Image) {
        ImageLoader.load(model.uri, itemView.iv_image,
            options = RequestOptions()
                .override(itemView.width, itemView.height)
                .centerCrop()
        )
    }

    private fun setLiked(isLiked: Boolean) {
        itemView.iv_like.setImageResource(if (isLiked) R.drawable.ic_like_red_36dp else R.drawable.ic_like_outline_white_36dp)
    }

    // --------------------------------------------------------------------------------------------
    private var animation: LikeAnimation? = null

    private fun showLikeAnimationSmall(liked: Boolean) {
        setLiked(liked)
        ScaleAnimation(0.7f, 1f, 0.7f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f)
            .apply {
                duration = 250
                interpolator = OvershootInterpolator()
            }.let { itemView.iv_like.startAnimation(it) }
    }

    private fun showLikeAnimation() {
        animation?.cancel()
        animation = LikeAnimation(itemView.iv_like_anim).apply { show() }
    }
}
