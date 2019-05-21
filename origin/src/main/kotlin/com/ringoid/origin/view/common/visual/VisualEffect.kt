package com.ringoid.origin.view.common.visual

import androidx.annotation.DrawableRes
import com.ringoid.origin.R

sealed class VisualEffect(val x: Float, val y: Float, @DrawableRes val resId: Int) {

    override fun toString(): String = "${javaClass.simpleName}($x, $y)"
}
class LikeVisualEffect(x: Float, y: Float) : VisualEffect(x, y, R.drawable.ic_visual_like_48)
class MatchVisualEffect(x: Float, y: Float) : VisualEffect(x, y, R.drawable.ic_visual_match_48)
