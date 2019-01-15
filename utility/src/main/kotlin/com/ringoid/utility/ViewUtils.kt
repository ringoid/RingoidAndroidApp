package com.ringoid.utility

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.TouchDelegate
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

// ------------------------------------------------------------------------------------------------
fun View.changeVisibility(isVisible: Boolean, soft: Boolean = false) {
    visibility = if (isVisible) View.VISIBLE else if (soft) View.INVISIBLE else View.GONE
}

fun View.touchExtend(size: Int = 100): View {
    (parent as? View)?.let { parent ->
        parent.post {
            val rect = Rect()
            getHitRect(rect)
            rect.left += size / 2
            rect.top += size / 2
            rect.right += size / 2
            rect.bottom += size / 2
            parent.touchDelegate = TouchDelegate(rect, this@touchExtend)
        }
    }
    return this
}

// ------------------------------------------------------------------------------------------------
@DrawableRes
fun Context.getSelectableItemBgResId(): Int {
    val tv = TypedValue()
    theme.resolveAttribute(android.R.attr.selectableItemBackground, tv, true)
    return tv.resourceId
}

fun Context.getSelectableItemBg(): Drawable? = ContextCompat.getDrawable(this, getSelectableItemBgResId())

// ------------------------------------------------------------------------------------------------
fun snackbar(view: View?, text: String, duration: Int = Snackbar.LENGTH_SHORT) {
    view?.let { Snackbar.make(it, text, duration).show() }
}

fun snackbar(view: View?, @StringRes textResId: Int, duration: Int = Snackbar.LENGTH_SHORT) {
    view?.let { Snackbar.make(it, textResId, duration).show() }
}
