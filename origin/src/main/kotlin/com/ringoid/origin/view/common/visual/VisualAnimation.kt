package com.ringoid.origin.view.common.visual

import android.view.animation.*

fun alphaIn(dur: Long = 250L, offset: Long = 0L, interp: Interpolator? = null): Animation =
    AlphaAnimation(0.5f, 0.9f).apply {
        duration = dur
        startOffset = offset
        interp?.let { interpolator = it }
    }
fun alphaOut(dur: Long = 80L, offset: Long = 0L, interp: Interpolator? = null): Animation =
    AlphaAnimation(0.9f, 0f).apply {
        duration = dur
        startOffset = offset
        interpolator = interp
    }
fun scaleUp(from: Float = 0.5f, dur: Long = 250L, interp: Interpolator? = null): Animation =
    ScaleAnimation(from, 1f, from, 1f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f)
        .apply {
            duration = dur
            interp?.let { interpolator = it }
        }
