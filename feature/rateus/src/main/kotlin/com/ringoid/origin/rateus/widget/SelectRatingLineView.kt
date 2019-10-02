package com.ringoid.origin.rateus.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * A [RatingLineView] with the ability to set rating value in runtime.
 * It is not possible to set non-integral rating value.
 */
class SelectRatingLineView : RatingLineView {

    interface OnRatingSelectListener {
        fun onRatingSelected(rating: Int)
    }

    private var listener: OnRatingSelectListener? = null

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        noRating()
    }

    /* API */
    // --------------------------------------------------------------------------------------------
    override fun noRating() {
        assignRating(0)  // selectable rating line should be visible
    }

    /* Listener */
    // ------------------------------------------
    fun setRatingSelectListener(listener: ((rating: Int) -> Unit)? = null) {
        this.listener = object : OnRatingSelectListener {
            override fun onRatingSelected(rating: Int) {
                listener?.invoke(rating)
            }
        }
    }

    fun setRatingSelectListener(listener: OnRatingSelectListener?) {
        this.listener = listener
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private fun assignRating(rating: Int) {
        ratingValue = rating.toFloat()

        var index = 0
        stars.forEach {
            if (index + 1 <= rating) {
                it.background = fullStar
            } else {
                it.background = noneStar
            }
            ++index
        }
    }

    @Suppress("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (event.x < stars[0].left) {
                    noRating()
                } else {
                    for (i in 0 until stars.size) {
                        if (event.x >= stars[i].left && (i >= stars.size - 1 || event.x < stars[i + 1].left)) {
                            assignRating(i + 1)
                        }
                    }
                }
                return true  // allow subsequent events
            }
            MotionEvent.ACTION_UP -> listener?.onRatingSelected(ratingValue.toInt())
        }
        return super.onTouchEvent(event)
    }
}
