package com.ringoid.origin.view.common.visual

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import com.ringoid.origin.AppRes
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.scope
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

class VisualEffectView : FrameLayout {

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        // no-op
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        VisualEffectManager.effect
            .observeOn(AndroidSchedulers.mainThread())
            .`as`(AutoDispose.autoDisposable(scope()))
            .subscribe({ playAnimation(it) }, Timber::e)
    }

    // ------------------------------------------
    private fun playAnimation(effect: VisualEffect) {
        Timber.v("VisualEffect: $effect")
        val image = ImageView(context)
            .apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                setImageResource(effect.resId)
//                left = (effect.x - AppRes.ICON_SIZE_HALF_96).toInt()
//                top = (effect.y - AppRes.ICON_SIZE_HALF_96).toInt()
//                translationX = effect.x - AppRes.ICON_SIZE_HALF_96
//                translationY = effect.y - AppRes.ICON_SIZE_HALF_96
                addView(this)
            }
        val animationSet = AnimationSet(false)
            .apply {
                addAnimation(alphaIn(0.5f, 0.9f, dur = 300L))
//                addAnimation(translateUp(effect.x, effect.y, 350f, 300L))
                addAnimation(scaleUp(interp = AccelerateInterpolator()))
                addAnimation(alphaOut(0.9f, 0f, offset = 300L, interp = DecelerateInterpolator()))
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) { /* no-op */ }
                    override fun onAnimationRepeat(animation: Animation) { /* no-op */ }
                    override fun onAnimationEnd(animation: Animation) {
                        removeView(image)
                    }
                })
            }
        image.post { image.startAnimation(animationSet) }
    }
}
