package com.ringoid.origin.feed.anim

import android.view.View
import android.view.animation.*
import com.ringoid.utility.changeVisibility

class LikeAnimation(private val view: View) {

    private val animation: AnimationSet

    init {
        view.changeVisibility(isVisible = true)

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

        animation = AnimationSet(false).apply {
            addAnimation(animationAlphaIn)
            addAnimation(animationResize)
            addAnimation(animationAlphaOut)
            fillAfter = true
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    this@LikeAnimation.cancel()
                }
            })
        }
    }

    fun cancel() {
        animation.cancel()
        view.apply {
            clearAnimation()
            changeVisibility(isVisible = false, soft = true)
        }
    }

    fun show() {
        animation.reset()
        view.startAnimation(animation)
    }
}
