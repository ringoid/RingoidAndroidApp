package com.ringoid.origin.view.common.visual

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.origin.AppRes
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.scope
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

class VisualEffectView : FrameLayout {

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Timber.v("VisualEffect: view attached")
        VisualEffectManager.effect
            .observeOn(AndroidSchedulers.mainThread())
            .doOnDispose { Timber.v("VisualEffect: disposed") }
            .`as`(AutoDispose.autoDisposable(scope()))
            .subscribe({ playAnimation(it) }, DebugLogUtil::e)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Timber.v("VisualEffect: view detached")
    }

    // ------------------------------------------
    private fun playAnimation(effect: VisualEffect) {
        Timber.v("VisualEffect: $effect")
        val image = ImageView(context)
            .apply {
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                setImageResource(effect.resId)
                translationX = effect.x - AppRes.ICON_SIZE_HALF2_96
                translationY = effect.y - AppRes.ICON_SIZE_96
                addView(this)
            }

        image.animate()
            .translationYBy(-1500f)
            .scaleXBy(2f)
            .scaleYBy(2f)
            .alpha(0f)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    removeView(image)
                }
            })
            .setDuration(1227L)
            .start()
    }
}
