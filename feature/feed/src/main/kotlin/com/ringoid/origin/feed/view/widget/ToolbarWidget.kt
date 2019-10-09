package com.ringoid.origin.feed.view.widget

import android.view.View
import android.view.animation.TranslateAnimation
import androidx.appcompat.widget.Toolbar
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.fragment_feed.view.*

internal class ToolbarWidget(private val rootView: View) {

    private var isVisibleAnimated: Boolean = true

    internal fun init(l: (toolbar: Toolbar) -> Unit): ToolbarWidget {
        l.invoke(rootView.toolbar)
        return this
    }

    internal fun hide() {
        if (!isShow()) {
            return
        }

        isVisibleAnimated = false
        TranslateAnimation(0f, 0f, 0f, -rootView.appbar.height.toFloat())
            .apply { duration = 400; fillAfter = true }
            .let { rootView.appbar.startAnimation(it) }

        rootView.appbar.changeVisibility(isVisible = false, soft = true)
    }

    internal fun show() {
        if (isShow()) {
            return
        }

        rootView.appbar.changeVisibility(isVisible = true)

        isVisibleAnimated = true
        TranslateAnimation(0f, 0f, -rootView.appbar.height.toFloat(), 0f)
            .apply { duration = 400; fillAfter = true }
            .let { rootView.appbar.startAnimation(it) }
    }

    private fun isShow(): Boolean = isVisibleAnimated
}
