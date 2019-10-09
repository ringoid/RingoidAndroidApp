package com.ringoid.origin.feed.view.widget

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import androidx.appcompat.widget.Toolbar
import com.ringoid.origin.AppRes
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
            AnimationSet(true).apply {
//                addAnimation(AlphaAnimation(1.0f, 0.0f))
                addAnimation(TranslateAnimation(0f, 0f, 0f, -AppRes.FEED_TOOLBAR_HEIGHT.toFloat()))
                duration = 400
                fillAfter = true
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}

                    override fun onAnimationEnd(animation: Animation) {
                        rootView.toolbar.changeVisibility(isVisible = false)
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
            }.let { rootView.toolbar.startAnimation(it) }
        }
    }

    internal fun show(animated: Boolean = true) {
        if (isShow()) {
            return
        }

        isVisibleAnimated = true

        if (animated) {
            AnimationSet(true).apply {
//                addAnimation(AlphaAnimation(0.0f, 1.0f))
                addAnimation(TranslateAnimation(0f, 0f, -AppRes.FEED_TOOLBAR_HEIGHT.toFloat(), 0f))
                duration = 400
                fillAfter = true
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}

                    override fun onAnimationEnd(animation: Animation) {
                        rootView.toolbar.changeVisibility(isVisible = true)
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
            }.let { rootView.toolbar.startAnimation(it) }
        }
    }

    internal fun isShow(): Boolean = isVisibleAnimated
}
