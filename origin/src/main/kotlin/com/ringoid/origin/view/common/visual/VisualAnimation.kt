package com.ringoid.origin.view.common.visual

import android.view.animation.*

fun alphaIn(): Animation = AlphaAnimation(0.5f, 0.9f).apply { duration = 250 }
fun alphaOut(): Animation = AlphaAnimation(0.9f, 0f).apply {
    duration = 80
    startOffset = 300
    interpolator = DecelerateInterpolator()
}
fun scaleUp(from: Float = 0.5f): Animation = ScaleAnimation(from, 1f, from, 1f,
    Animation.RELATIVE_TO_SELF, 0.5f,
    Animation.RELATIVE_TO_SELF, 0.5f)
    .apply {
        duration = 250
        interpolator = OvershootInterpolator()
    }
