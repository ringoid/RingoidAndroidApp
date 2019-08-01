package com.ringoid.origin.feed.adapter.base

import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.OvershootInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.origin.view.common.visual.scaleUp
import kotlinx.android.synthetic.main.rv_item_feed_profile_content.view.*

class LikeFeedItemAnimator : FeedItemAnimator() {

    private val animationsMapLikeButton: MutableMap<RecyclerView.ViewHolder, AnimationSet> = mutableMapOf()
    private val animationsMapLikeImage: MutableMap<RecyclerView.ViewHolder, AnimationSet> = mutableMapOf()

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean = true

    override fun recordPreLayoutInformation(state: RecyclerView.State,
            viewHolder: RecyclerView.ViewHolder, changeFlags: Int, payloads: List<Any>): ItemHolderInfo {
        if (changeFlags == RecyclerView.ItemAnimator.FLAG_CHANGED) {
            for (payload in payloads) {
                if (payload is FeedViewHolderAnimatePayload) {
                    return ViewHolderInfo(payload)
                }
            }
        }
        return super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads)
    }

    override fun animateChange(oldHolder: RecyclerView.ViewHolder, newHolder: RecyclerView.ViewHolder,
                               preInfo: ItemHolderInfo, postInfo: ItemHolderInfo): Boolean {
        cancelCurrentAnimationIfExists(newHolder)

        if (preInfo is ViewHolderInfo) {
            if (preInfo.payload is FeedItemViewHolderAnimateLike) {
                animateButtonLike(newHolder)
            }
        }
        return false
    }

    private fun cancelCurrentAnimationIfExists(item: RecyclerView.ViewHolder) {
        animationsMapLikeImage[item]?.cancel()
        animationsMapLikeButton[item]?.cancel()
    }

    private fun animateButtonLike(holder: RecyclerView.ViewHolder) {
        val animationSet = AnimationSet(false)
            .apply {
                addAnimation(scaleUp(from = 0.7f, interp = OvershootInterpolator()))
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) { /* no-op */ }
                    override fun onAnimationRepeat(animation: Animation) { /* no-op */ }
                    override fun onAnimationEnd(animation: Animation) {
                        animationsMapLikeButton.remove(holder)
                        dispatchChangeFinishedIfAllAnimationsEnded(holder)
                    }
                })
            }
            .also { holder.itemView.ibtn_like.startAnimation(it) }

        animationsMapLikeButton[holder] = animationSet
    }

    private fun dispatchChangeFinishedIfAllAnimationsEnded(holder: RecyclerView.ViewHolder) {
        if (animationsMapLikeButton.containsKey(holder) || animationsMapLikeImage.containsKey(holder)) {
            return
        }

        dispatchAnimationFinished(holder)
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

class ViewHolderInfo(val payload: FeedViewHolderAnimatePayload) : RecyclerView.ItemAnimator.ItemHolderInfo()
