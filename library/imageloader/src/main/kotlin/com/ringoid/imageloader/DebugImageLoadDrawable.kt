package com.ringoid.imageloader

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import com.facebook.drawee.drawable.DrawableUtils
import com.ringoid.utility.splitBySegments

// debug only
class DebugImageLoadDrawable(private val cause: Throwable) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        textSize = 48f
    }

    @Suppress("CanvasSize")
    override fun draw(canvas: Canvas) {
        with (canvas) {
            cause.message?.let { msg ->
                msg.splitBySegments(32)
                    .forEachIndexed { index, s -> drawText(s, 64f, 64f + 64f * index, paint) }
            }
            drawText(cause.javaClass.simpleName, canvas.width * 0.4f, canvas.height * 0.45f, paint)
        }
    }

    override fun getOpacity(): Int =
        DrawableUtils.getOpacityFromColor(paint.color)

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter) {
        paint.colorFilter = colorFilter
    }
}
