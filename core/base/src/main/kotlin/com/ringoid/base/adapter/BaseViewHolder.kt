package com.ringoid.base.adapter

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.utility.clickDebounce

abstract class BaseViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {

    abstract fun bind(model: T)
    open fun bind(model: T, payloads: List<Any>) {
        /**
         * This is only called on explicit call of notify**Changed() with payloads and must only
         * perform some changes based on payloads. There must not be any fallback to [bind] without
         * payloads, and [bind] without payloads must drop any effect caused by applied payloads -
         * because such [BaseViewHolder] could come from Recycled View Pool and there could be applied
         * payloads - they must be cancelled on rebind and applied explicitly after, on user demand.
         */
        // override in subclasses
    }

    @Suppress("CheckResult")
    fun setOnClickListener(l: View.OnClickListener?) {
        itemView.apply {
            isClickable = l != null
            clicks().compose(clickDebounce()).subscribe { l?.onClick(this) }

            // detect touch on viewHolder and call visual effect at touch point
            setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    view.tag = event
                }
                false
            }
        }
    }

    @Suppress("CheckResult")
    fun setOnDoubleClickListener(l: View.OnClickListener?) {
        val cb = object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTapEvent(event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_UP) {
                    l?.onClick(itemView)
                }
                return true
            }
        }
        val gestureDetector = GestureDetectorCompat(itemView.context, cb)
        itemView.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                view.tag = event
            }
            gestureDetector.onTouchEvent(event)
        }
    }
}
