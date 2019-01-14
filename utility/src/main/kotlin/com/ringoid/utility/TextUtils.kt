package com.ringoid.utility

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
            if (link.size != 0) {
                processUrl(link[0].url)
                return true
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }
}
