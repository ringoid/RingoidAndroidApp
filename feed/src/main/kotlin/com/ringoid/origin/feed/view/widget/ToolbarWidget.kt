package com.ringoid.origin.feed.view.widget

import android.view.View
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_feed.view.*

class ToolbarWidget(private val rootView: View) {

    internal fun isShow(): Boolean = rootView.appbar.height - rootView.appbar.bottom <= 0

    internal fun removeScrollFlags() {
        rootView.toolbar?.let { v ->
            v.layoutParams = v.layoutParams
                ?.let { it as? AppBarLayout.LayoutParams }
                ?.let { lp -> lp.scrollFlags = 0; lp }
        }
    }

    internal fun restoreScrollFlags() {
        rootView.toolbar?.let { v ->
            v.layoutParams = v.layoutParams
                ?.let { it as? AppBarLayout.LayoutParams }
                ?.let { lp ->
                    lp.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                            AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP or
                            AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                    lp
                }
        }
    }

    internal fun show(isVisible: Boolean) {
        rootView.appbar?.let {
            if (isShow()) {
                if (isVisible) return
            } else {
                if (!isVisible) return
            }
            it.setExpanded(isVisible)
        }
    }
}
