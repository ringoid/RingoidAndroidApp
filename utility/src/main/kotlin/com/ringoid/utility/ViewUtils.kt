package com.ringoid.utility

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

fun View.changeVisibility(isVisible: Boolean, soft: Boolean = false) {
    visibility = if (isVisible) View.VISIBLE else if (soft) View.INVISIBLE else View.GONE
}

@DrawableRes
fun Context.getSelectableItemBgResId(): Int {
    val tv = TypedValue()
    theme.resolveAttribute(android.R.attr.selectableItemBackground, tv, true)
    return tv.resourceId
}

fun Context.getSelectableItemBg(): Drawable? = ContextCompat.getDrawable(this, getSelectableItemBgResId())
