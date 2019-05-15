package com.ringoid.origin.view.common.visual

import androidx.annotation.DrawableRes
import com.ringoid.origin.R

sealed class VisualEffect(val x: Int, val y: Int, @DrawableRes val resId: Int)
class LikeVisualEffect(x: Int, y: Int) : VisualEffect(x, y, R.drawable.ic_visual_like)
class MatchVisualEffect(x: Int, y: Int) : VisualEffect(x, y, R.drawable.ic_visual_match)
