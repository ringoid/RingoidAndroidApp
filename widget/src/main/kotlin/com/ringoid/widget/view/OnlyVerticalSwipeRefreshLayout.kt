package com.ringoid.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class OnlyVerticalSwipeRefreshLayout(context: Context, attrs: AttributeSet) : SwipeRefreshLayout(context, attrs) {

    private val touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    private var prev: MotionEvent? = null
    private var declined: Boolean = false

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                prev = MotionEvent.obtain(event)
                declined = false  // new action
            }
            MotionEvent.ACTION_MOVE -> {
                val xDiff = Math.abs(event.x - (prev?.x ?: 0f))
                if (declined || xDiff > touchSlop) {
                    declined = true  // memorize
                    return false
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                prev?.recycle()
                prev = null
            }
        }
        return super.onInterceptTouchEvent(event)
    }
}
