package com.ringoid.origin.feed.adapter.profile

import android.view.animation.*
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.rv_item_profile_image.view.*

class ProfileImageItemAnimator : DefaultItemAnimator() {

    private val animationsMapLikeButton: MutableMap<RecyclerView.ViewHolder, AnimationSet> = mutableMapOf()
    private val animationsMapLikeImage: MutableMap<RecyclerView.ViewHolder, AnimationSet> = mutableMapOf()

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean = true
    
    override fun recordPreLayoutInformation(state: RecyclerView.State,
        viewHolder: RecyclerView.ViewHolder, changeFlags: Int, payloads: List<Any>)
            : RecyclerView.ItemAnimator.ItemHolderInfo {

        if (changeFlags == RecyclerView.ItemAnimator.FLAG_CHANGED) {
            for (payload in payloads) {
                if (payload is ProfileImageViewHolderPayload) {
                    return ProfileImageViewHolderInfo(payload)
                }
            }
        }
        return super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads)
    }

    override fun animateChange(oldHolder: RecyclerView.ViewHolder, newHolder: RecyclerView.ViewHolder,
        preInfo: RecyclerView.ItemAnimator.ItemHolderInfo, postInfo: RecyclerView.ItemAnimator.ItemHolderInfo): Boolean {

        cancelCurrentAnimationIfExists(newHolder)

        if (preInfo is ProfileImageViewHolderInfo) {
            (newHolder as? ProfileImageViewHolder)?.let { vh ->
                if (preInfo.payload is ProfileImageViewHolderAnimateLike) {
                    animateButtonLike(vh, isLiked = true)
                    animateImageLike(vh)
                }
                if (preInfo.payload is ProfileImageViewHolderAnimateLikeButton) {
                    animateButtonLike(vh, isLiked = true)
                }
                if (preInfo.payload is ProfileImageViewHolderAnimateUnLikeButton) {
                    vh.setLiked(isLiked = false)
                }
            }
        }

        return false
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean = false
    override fun animateMove(holder: RecyclerView.ViewHolder?, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean = false
    override fun animateRemove(holder: RecyclerView.ViewHolder?): Boolean = false

    private fun cancelCurrentAnimationIfExists(item: RecyclerView.ViewHolder) {
        animationsMapLikeImage[item]?.cancel()
        animationsMapLikeButton[item]?.cancel()
    }

    private fun animateButtonLike(holder: ProfileImageViewHolder, isLiked: Boolean) {
        val animation = ScaleAnimation(0.7f, 1f, 0.7f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f)
            .apply {
                duration = 250
                interpolator = OvershootInterpolator()
            }

        val animationSet = AnimationSet(false)
            .apply {
                addAnimation(animation)
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) { holder.setLiked(isLiked) }
                    override fun onAnimationRepeat(animation: Animation) { /* no-op */ }
                    override fun onAnimationEnd(animation: Animation) {
                        animationsMapLikeButton.remove(holder)
                        dispatchChangeFinishedIfAllAnimationsEnded(holder)
                    }
                })
            }
//            .also { holder.itemView.ibtn_like.startAnimation(it) }

        animationsMapLikeButton[holder] = animationSet
    }

    private fun animateImageLike(holder: ProfileImageViewHolder) {
        val animationAlphaIn = AlphaAnimation(0.5f, 0.9f).apply { duration = 250 }

        val animationResize = ScaleAnimation(0.5f, 1f, 0.5f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f)
            .apply {
                duration = 250
                interpolator = OvershootInterpolator()
            }

        val animationAlphaOut = AlphaAnimation(0.9f, 0f).apply {
            duration = 80
            startOffset = 300
            interpolator = DecelerateInterpolator()
        }

        val animationSet = AnimationSet(false)
            .apply {
                addAnimation(animationAlphaIn)
                addAnimation(animationResize)
                addAnimation(animationAlphaOut)
                fillAfter = true
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) { /* no-op */ }
                    override fun onAnimationRepeat(animation: Animation) { /* no-op */ }
                    override fun onAnimationEnd(animation: Animation) {
                        animationsMapLikeImage.remove(holder)
                        resetLikeAnimationState(holder)
                        dispatchChangeFinishedIfAllAnimationsEnded(holder)
                    }
                })
            }
            .also { holder.itemView.iv_like_anim.startAnimation(it) }

        animationsMapLikeImage[holder] = animationSet
    }

    private fun dispatchChangeFinishedIfAllAnimationsEnded(holder: ProfileImageViewHolder) {
        if (animationsMapLikeButton.containsKey(holder) || animationsMapLikeImage.containsKey(holder)) {
            return
        }

        dispatchAnimationFinished(holder)
    }

    private fun resetLikeAnimationState(holder: ProfileImageViewHolder) {
        holder.itemView.iv_like_anim.changeVisibility(isVisible = false, soft = true)
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        super.endAnimation(item)
        cancelCurrentAnimationIfExists(item)
    }

    override fun endAnimations() {
        super.endAnimations()
        for (animatorSet in animationsMapLikeImage.values) {
            animatorSet.cancel()
        }
    }
}

class ProfileImageViewHolderInfo(val payload: ProfileImageViewHolderPayload) : RecyclerView.ItemAnimator.ItemHolderInfo()
