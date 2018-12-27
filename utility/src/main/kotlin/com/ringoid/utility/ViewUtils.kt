package com.ringoid.utility

import android.view.View

fun View.changeVisibility(isVisible: Boolean, soft: Boolean = false) {
    visibility = if (isVisible) View.VISIBLE else if (soft) View.INVISIBLE else View.GONE
}
