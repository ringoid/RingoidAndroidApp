package com.ringoid.utility.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import timber.log.Timber

fun getBitmap(context: Context, @DrawableRes resId: Int): Bitmap? =
    ContextCompat.getDrawable(context, resId)?.let {
        when (it) {
            is BitmapDrawable -> it.bitmap
            is VectorDrawable -> getBitmap(it)
            else -> {
                Timber.e("Unsupported drawable type: ${it.javaClass.simpleName}")
                null
            }
        }
    }

fun getBitmap(drawable: VectorDrawable): Bitmap {
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
