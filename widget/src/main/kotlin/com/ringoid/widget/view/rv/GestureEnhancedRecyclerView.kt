package com.ringoid.widget.view.rv

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class GestureEnhancedRecyclerView : RecyclerView {

    private lateinit var gestureDetector: GestureDetector

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        gestureDetector = GestureDetector(context, YScrollDetector())
        setFadingEdgeLength(0)
    }

    // --------------------------------------------------------------------------------------------
    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        // call super first because it does some hidden motion event handling
        val result = super.onInterceptTouchEvent(e)

        // now see whether we are scrolling vertically with the custom gesture detector
        return result.takeIf { gestureDetector.onTouchEvent(e) } ?: false
        // if not scrolling vertically (more y than x), don't hijack the event.
    }
}

/**
 * Returns false if we're scrolling in the x direction.
 */
class YScrollDetector : GestureDetector.SimpleOnGestureListener() {

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean =
        try {
            Math.abs(distanceY) > Math.abs(distanceX)
        } catch (e: Throwable) {
            false
        }
}
