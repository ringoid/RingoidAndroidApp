package com.ringoid.utility

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView

abstract class AutoLinkMovementMethod : LinkMovementMethod() {

    abstract fun processUrl(url: String)

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.action

        if (action == MotionEvent.ACTION_UP) {
            var x = event.x.toInt()
            var y = event.y.toInt()

            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop

            x += widget.scrollX
            y += widget.scrollY

            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())

            val link = buffer.getSpans(off, off, URLSpan::class.java)
            if (link.isNotEmpty()) {
                processUrl(link[0].url)
                return true
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }
}

/* Clipboard */
// --------------------------------------------------------------------------------------------
fun Context.copyToClipboard(key: String, value: String) {
    (getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager)
        ?.primaryClip = ClipData.newPlainText(key, value)
}

fun Context.pasteFromClipboard(): String {
    val stringBuilder = StringBuilder()
    (getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager)
        ?.primaryClip
        ?.let {
            for (i in 0 until it.itemCount) {
                stringBuilder.append(it.getItemAt(i).text)
            }
        }
    return stringBuilder.toString()
}

/* Keyboard */
// --------------------------------------------------------------------------------------------
fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
}

fun EditText.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_FORCED)
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun Window.showKeyboard() {
    setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
}

fun Window.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(decorView.windowToken, 0)
}

/* Span */
// --------------------------------------------------------------------------------------------
fun TextView.highlightFrom(start: Int, textColor: Int) {
    if (start != -1) {
        SpannableString(text).apply {
            setSpan(ForegroundColorSpan(textColor), start, text.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            text = this
        }
    }
}
