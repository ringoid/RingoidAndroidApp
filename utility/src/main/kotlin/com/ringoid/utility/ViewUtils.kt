package com.ringoid.utility

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.TouchDelegate
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

// ------------------------------------------------------------------------------------------------
fun View.changeVisibility(isVisible: Boolean, soft: Boolean = false) {
    visibility = if (isVisible) View.VISIBLE else if (soft) View.INVISIBLE else View.GONE
}

fun View.getScreenshot(view: View): Bitmap {
    view.isDrawingCacheEnabled = true
    val bmp = Bitmap.createBitmap(view.drawingCache)
    view.isDrawingCacheEnabled = false
    return bmp
}

fun View.touchExtend(size: Int = 100): View {
    (parent as? View)?.let { parent ->
        parent.post {
            val rect = Rect()
            this@touchExtend.getHitRect(rect)
            rect.left -= size / 2
            rect.top -= size / 2
            rect.right += size / 2
            rect.bottom += size / 2
            parent.touchDelegate = TouchDelegate(rect, this@touchExtend)
        }
    }
    return this
}

fun TextView.changeTypeface(tf: Typeface? = null, style: Int = Typeface.NORMAL, isSelected: Boolean = false) {
    this.isSelected = isSelected
    setTypeface(tf, style)
}

// ------------------------------------------------------------------------------------------------
@DrawableRes
fun Context.getSelectableItemBgResId(): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
    return typedValue.resourceId
}

fun Context.getSelectableItemBg(): Drawable? = ContextCompat.getDrawable(this, getSelectableItemBgResId())

fun Context.getAttributeColor(attributeId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attributeId, typedValue, true)
    return ContextCompat.getColor(this, typedValue.resourceId)
}

fun Context.getAttributeDrawable(attributeId: Int): Drawable? {
    val typedValue = TypedValue()
    theme.resolveAttribute(attributeId, typedValue, true)
    return ContextCompat.getDrawable(this, typedValue.resourceId)
}

fun Context.getAttributeDimension(attributeId: Int): Float {
    val typedValue = TypedValue()
    theme.resolveAttribute(attributeId, typedValue, true)
    return resources.getDimension(typedValue.resourceId)
}

fun Activity.getScreenDensity(): Float {
    val metrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(metrics)
    return metrics.density
}

/**
 * Get screen smallest width in pixels.
 */
// {@see http://stackoverflow.com/questions/15055458/detect-7-inch-and-10-inch-tablet-programmatically}
fun Activity.getSmallestWidth(): Float {
    val metrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(metrics)
    val widthPixels = metrics.widthPixels
    val heightPixels = metrics.heightPixels
    val scaleFactor = metrics.density
    val widthDp = widthPixels / scaleFactor
    val heightDp = heightPixels / scaleFactor
    return Math.min(widthDp, heightDp)
}

/**
 * Get screen width in pixels.
 */
fun Activity.getScreenWidth(): Int {
    val metrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(metrics)
    return metrics.widthPixels
}

fun Activity.getScreenWidthDp(): Float = getScreenWidth() / getScreenDensity()

/**
 * Get screen height in pixels.
 */
fun Activity.getScreenHeight(): Int {
    val metrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(metrics)
    return metrics.heightPixels
}

fun Activity.getScreenHeightDp(): Float = getScreenHeight() / getScreenDensity()

/**
 * Retrieves [Bitmap] from file specified with {@param path}, resized to adopt
 * the {@param targetWidth} and {@param targetHeight}.
 *
 * {@see https://developer.android.com/training/camera/photobasics.html}
 */
fun getBitmapFromFile(path: String?, targetWidth: Int, targetHeight: Int): Bitmap? {
    if (path.isNullOrBlank()) return null

    // Get the dimensions of the bitmap
    val bmOptions = BitmapFactory.Options()
    bmOptions.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, bmOptions)
    val photoW = bmOptions.outWidth
    val photoH = bmOptions.outHeight

    // Determine how much to scale down the image
    val scaleFactor = Math.min(photoW / targetWidth, photoH / targetHeight)

    // Decode the image file into a Bitmap sized to fill the View
    bmOptions.apply {
        inJustDecodeBounds = false
        inSampleSize = scaleFactor
        inPurgeable = true
    }

    return BitmapFactory.decodeFile(path, bmOptions)
}

// ------------------------------------------------------------------------------------------------
fun snackbar(view: View?, text: String, duration: Int = Snackbar.LENGTH_SHORT) {
    view?.let { Snackbar.make(it, text, duration).show() }
}

fun snackbar(view: View?, @StringRes textResId: Int, duration: Int = Snackbar.LENGTH_SHORT) {
    view?.let { Snackbar.make(it, textResId, duration).show() }
}

fun Context.toast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Context.toast(@StringRes textResId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, textResId, duration).show()
}
