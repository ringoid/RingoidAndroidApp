package com.ringoid.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * @see https://stackoverflow.com/questions/29442216/how-to-disable-or-enable-viewpager-swiping-in-android/42687474
 * @see https://www.oodlestechnologies.com/blogs/Stop-Swipe-Action-in-Android-Viewpager
 */
class DisableSwipeViewPager : ViewPager {

    private var disable: Boolean = true

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): super(context, attributes)

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean =
        if (disable) false else super.onInterceptTouchEvent(event)

    override fun onTouchEvent(event: MotionEvent): Boolean =
        if (disable) false else super.onTouchEvent(event)
}
