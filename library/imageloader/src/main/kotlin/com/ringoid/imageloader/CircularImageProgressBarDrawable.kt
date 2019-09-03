package com.ringoid.imageloader

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import com.facebook.drawee.drawable.DrawableUtils

class CircularImageProgressBarDrawable : Drawable() {

    companion object {
        const val RADIUS = 150f
        const val WIDTH = 32f
    }

    private var xlevel: Int = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#a1bdc1c6")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
        strokeWidth = WIDTH
    }

    @Suppress("CanvasSize")
    override fun draw(canvas: Canvas) {
        with (canvas) {
            drawArc(
                canvas.width * 0.5f - RADIUS,
                canvas.height * 0.5f - RADIUS,
                canvas.width * 0.5f + RADIUS,
                canvas.height * 0.5f + RADIUS,
                0f, 0.036f * xlevel, false, paint)
        }
    }

    override fun getOpacity(): Int =
        DrawableUtils.getOpacityFromColor(paint.color)

    override fun onLevelChange(level: Int): Boolean {
        xlevel = level
        invalidateSelf()
        return true
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter) {
        paint.colorFilter = colorFilter
    }
}
