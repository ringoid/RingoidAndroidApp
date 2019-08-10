package com.ringoid.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.abs

class SwipelessExtendImageButton : ExtendImageButton {

    private val touchSlop: Int
    private var isSwiping: Boolean = false
    private var xStart: Float = 0.0f

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr) {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean =
        when (ev.actionMasked) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (!isSwiping) {
                    performClick()
                }
                isSwiping = false
                false
            }
            MotionEvent.ACTION_DOWN -> {
                xStart = ev.x
                false
            }
            MotionEvent.ACTION_MOVE -> {
                if (isSwiping) {
                    true
                } else {
                    val xDiff = calculateDistanceX(ev)
                    if (xDiff > touchSlop) {
                        isSwiping = true
                        true
                    } else {
                        false
                    }
                }
            }
            else -> false
        }

    override fun onTouchEvent(event: MotionEvent): Boolean =
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> false
            else -> super.onTouchEvent(event)
        }

    // --------------------------------------------------------------------------------------------
    private fun calculateDistanceX(event: MotionEvent): Float = abs(xStart - event.x)
}
