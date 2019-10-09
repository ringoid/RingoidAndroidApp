package com.ringoid.origin.feed.view.widget

import android.view.View
import android.view.animation.Animation
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

    internal fun hide(animated: Boolean = true) {
        if (!isShow()) {
            return
        }

        isVisibleAnimated = false

        if (animated) {
            TranslateAnimation(0f, 0f, 0f, -rootView.appbar.height.toFloat())
                .apply {
                    duration = 400
                    fillAfter = true
                    setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {}

                        override fun onAnimationEnd(animation: Animation) {
                            rootView.appbar.changeVisibility(isVisible = false)
                        }

                        override fun onAnimationRepeat(animation: Animation) {}
                    })
                }
                .let { rootView.appbar.startAnimation(it) }
        }
    }

    internal fun show(animated: Boolean = true) {
        if (isShow()) {
            return
        }

        isVisibleAnimated = true

        if (animated) {
            TranslateAnimation(0f, 0f, -rootView.appbar.height.toFloat(), 0f)
                .apply {
                    duration = 400
                    fillAfter = true
                    setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {}

                        override fun onAnimationEnd(animation: Animation) {
                            rootView.appbar.changeVisibility(isVisible = true)
                        }

                        override fun onAnimationRepeat(animation: Animation) {}
                    })
                }
                .let { rootView.appbar.startAnimation(it) }
        }
    }

    internal fun isShow(): Boolean = isVisibleAnimated
}
