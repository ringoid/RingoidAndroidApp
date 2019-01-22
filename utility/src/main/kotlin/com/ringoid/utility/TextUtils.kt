package com.ringoid.utility

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.MotionEvent
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

fun Context.pasteFromClipboard(key: String): String {
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
