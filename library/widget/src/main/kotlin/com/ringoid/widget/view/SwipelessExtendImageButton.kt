package com.ringoid.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.annotation.LayoutRes
import com.ringoid.widget.R
import timber.log.Timber
import kotlin.math.abs

class SwipelessExtendImageButton : ExtendImageButton {

    private val touchSlop: Int
    private var isSwiping: Boolean = false
    private var startX: Float = 0.0f

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr) {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    @LayoutRes override fun getLayoutId(): Int = R.layout.widget_swipeless_extend_image_button

    override fun onTouchEvent(ev: MotionEvent): Boolean =
        when (ev.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                if (isSwiping) {
                    Timber.w("SWIPE: swipe handle")
                    false  // dispatch swipe gesture to parent
                } else super.onTouchEvent(ev)
            }
            else -> {
                isSwiping = false
                super.onTouchEvent(ev)  // handle any other gesture normally
            }
        }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean =
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isSwiping = false
                startX = ev.rawX
                Timber.d("SWIPE: DOWN $startX")
                false  // continue watching gesture here in onInterceptTouchEvent()
            }
            MotionEvent.ACTION_MOVE -> {
                val xDiff = abs(startX - ev.rawX)
                Timber.v("SWIPE: MOVE ${ev.rawX}, $xDiff / $touchSlop")
                if (xDiff >= touchSlop) {
                    Timber.i("SWIPE: SWIPING")
                    isSwiping = true
                    true  // continue watching on gesture in onTouchEvent()
                } else {
                    false  // continue watching gesture here in onInterceptTouchEvent()
                }
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                Timber.d("SWIPE: UP $isSwiping")
                isSwiping = false
                false
            }  // handle touch (click) normally
            else -> super.onInterceptTouchEvent(ev)
        }
}
