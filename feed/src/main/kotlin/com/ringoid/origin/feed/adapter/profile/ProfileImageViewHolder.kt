package com.ringoid.origin.feed.adapter.profile

import android.view.View
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.ScaleAnimation
import com.bumptech.glide.request.RequestOptions
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideControls
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowControls
import com.ringoid.origin.feed.anim.LikeAnimation
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.ImageLoader
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.rv_item_profile_image.view.*

interface IProfileImageViewHolder {

    fun animateLike(isLiked: Boolean)

    fun cancelAnimations()
}

abstract class BaseProfileImageViewHolder(view: View) : BaseViewHolder<ProfileImageVO>(view), IProfileImageViewHolder

class ProfileImageViewHolder(view: View, private val isLikeEnabled: Boolean = true) : BaseProfileImageViewHolder(view) {

    init {
        itemView.ibtn_like.changeVisibility(isVisible = isLikeEnabled)
    }

    override fun bind(model: ProfileImageVO) {
        showControls()  // cancel any effect caused by applied payloads
        ImageLoader.load(model.image.uri, itemView.iv_image,
            options = RequestOptions()
                .override(itemView.width, itemView.height)
                .centerCrop())

        setLiked(isLiked = model.isLiked)  // TODO: use payload
    }

    override fun bind(model: ProfileImageVO, payloads: List<Any>) {
        if (payloads.contains(FeedViewHolderHideControls)) {
            hideControls()
        }
        if (payloads.contains(FeedViewHolderShowControls)) {
            showControls()
        }
    }

    private fun setLiked(isLiked: Boolean) {
        itemView.ibtn_like.setImageResource(if (isLiked) R.drawable.ic_like_red_36dp else R.drawable.ic_like_outline_white_36dp)
    }

    private fun hideControls() {
        itemView.ibtn_like.changeVisibility(isVisible = false)
    }

    private fun showControls() {
        itemView.ibtn_like.changeVisibility(isVisible = isLikeEnabled)
    }

    // --------------------------------------------------------------------------------------------
    private var animation: LikeAnimation? = null

    override fun animateLike(isLiked: Boolean) {
        if (isLiked) showLikeAnimation()
        showLikeAnimationSmall(isLiked)
    }

    override fun cancelAnimations() {
        animation?.cancel()
    }

    private fun showLikeAnimationSmall(isLiked: Boolean) {
        setLiked(isLiked = isLiked)
        ScaleAnimation(0.7f, 1f, 0.7f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f)
            .apply {
                duration = 250
                interpolator = OvershootInterpolator()
            }.let { itemView.ibtn_like.startAnimation(it) }
    }

    private fun showLikeAnimation() {
        cancelAnimations()
        animation = LikeAnimation(itemView.iv_like_anim).apply { show() }
    }
}

class HeaderProfileImageViewHolder(view: View) : BaseProfileImageViewHolder(view) {

    override fun animateLike(isLiked: Boolean) {
        // no-op
    }

    override fun cancelAnimations() {
        // no-op
    }

    override fun bind(model: ProfileImageVO) {
        // no-op
    }
}
