package com.ringoid.widget.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.Button
import androidx.core.content.ContextCompat
import com.ringoid.widget.R

class BadgeButton : Button {

    private var badgeX: Float = 0.0f
    private var badgeY: Float = 0.0f
    private val badgePaint = Paint()
    private var badgeRadius: Float = 28.0f
    private var isBadgeVisible: Boolean = false
    private var bounds = Rect()

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        badgePaint.apply {
            color = ContextCompat.getColor(context, R.color.red_love)
            style = Paint.Style.FILL
        }
        badgeRadius = resources.getDimensionPixelSize(R.dimen.std_badge_radius).toFloat()
    }

    // --------------------------------------------------------------------------------------------
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isBadgeVisible) {
            paint.getTextBounds(text.toString(), 0, text.length, bounds)
            badgeX = (width + paint.measureText(text.toString())) * 0.5f + 3.0f
            badgeY = bounds.height().toFloat() + 12.0f
            canvas.drawCircle(badgeX, badgeY, badgeRadius, badgePaint)
        }
    }

    // ------------------------------------------
    fun showBadge(isVisible: Boolean) {
        isBadgeVisible = isVisible
        postInvalidate()
    }
}
