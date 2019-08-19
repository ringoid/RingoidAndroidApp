package com.ringoid.utility

import android.app.Activity
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

// ------------------------------------------------------------------------------------------------
fun View.changeVisibility(isVisible: Boolean, soft: Boolean = false) {
    if (isVisible != this.isVisible()) {
        visibility = if (isVisible) View.VISIBLE else if (soft) View.INVISIBLE else View.GONE
    }
}

fun View.isVisible(): Boolean = visibility == View.VISIBLE

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

fun RecyclerView.isVisibleToUser(vh: RecyclerView.ViewHolder): Boolean =
        vh.adapterPosition
            .takeIf { it != RecyclerView.NO_POSITION }
            ?.let { position ->
                linearLayoutManager()?.let {
                    val from = it.findFirstVisibleItemPosition()
                    val to = it.findLastVisibleItemPosition()
                    position in from..to
                } ?: false
            }
            ?: false
fun RecyclerView.linearLayoutManager(): LinearLayoutManager? = layoutManager as? LinearLayoutManager

fun TextView.changeTypeface(tf: Typeface? = null, style: Int = Typeface.NORMAL, isSelected: Boolean = false, textSize: Int? = null) {
    this.isSelected = isSelected
    setTypeface(tf, style)
    textSize?.let { setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat()) }
}

// ------------------------------------------------------------------------------------------------
fun dpToPx(dp: Float): Int = (dp * Resources.getSystem().displayMetrics.density).toInt()

fun pxToDp(px: Int): Float = px.toFloat() / Resources.getSystem().displayMetrics.density

@DrawableRes
fun Context.getSelectableItemBgResId(): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
    return typedValue.resourceId
}

@DrawableRes
fun Context.getSelectableItemBgBorderlessResId(): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true)
    return typedValue.resourceId
}

fun Context.getSelectableItemBg(): Drawable? = ContextCompat.getDrawable(this, getSelectableItemBgResId())
fun Context.getSelectableItemBgBorderless(): Drawable? = ContextCompat.getDrawable(this, getSelectableItemBgBorderlessResId())

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

fun Context.debugToast(text: String, duration: Int = Toast.LENGTH_SHORT, gravity: Int? = null) {
    if (BuildConfig.DEBUG) {
        toast(text, duration, gravity)
    }
}

fun Context.debugToast(@StringRes textResId: Int, duration: Int = Toast.LENGTH_SHORT, gravity: Int? = null) {
    if (BuildConfig.DEBUG) {
        toast(textResId, duration, gravity)
    }
}

fun Context.toast(text: String, duration: Int = Toast.LENGTH_SHORT, gravity: Int? = null) {
    val view = LayoutInflater.from(this).inflate(R.layout.toast_layout, null)
        .also { it.findViewById<TextView>(R.id.tv_toast_text).text = text }

    Toast.makeText(this, text, duration)
        .apply { gravity?.let { setGravity(gravity, 0, 0) } }
        .also { it.view = view }
        .show()
}

fun Context.toast(@StringRes textResId: Int, duration: Int = Toast.LENGTH_SHORT, gravity: Int? = null) {
    toast(text = resources.getString(textResId), duration = duration, gravity = gravity)
}

// ------------------------------------------------------------------------------------------------
const val VIBRATE_DURATION = 200L

fun Context.vibrate() {
    (getSystemService(VIBRATOR_SERVICE) as? Vibrator)?.let { vibrator ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATE_DURATION, 255))
        } else {
            vibrator.vibrate(VIBRATE_DURATION)
        }
    }
}
