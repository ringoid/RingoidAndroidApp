package com.ringoid.base.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.utility.clickDebounce
import timber.log.Timber

abstract class BaseViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {

    private var clickListener: View.OnClickListener? = null
    private var doubleClickListener: View.OnClickListener? = null

    init {
        Timber.v("Create ViewHolder: ${javaClass.simpleName}")
    }

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
            clickListener = l
            clicks().compose(clickDebounce()).subscribe { l?.onClick(this) }
        }
    }

    @Suppress("CheckResult")
    fun setOnDoubleClickListener(l: View.OnClickListener?) {
//        val cb = object : GestureDetector.SimpleOnGestureListener() {
//            override fun onDoubleTapEvent(event: MotionEvent): Boolean {
//                if (event.action == MotionEvent.ACTION_UP) {
//                    l?.onClick(itemView)
//                }
//                return true
//            }
//        }
//        val gestureDetector = GestureDetectorCompat(itemView.context, cb)
//        itemView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }

        doubleClickListener = l
        itemView.clicks()
            .doOnNext { clickListener?.onClick(itemView) }
            .buffer(2)
            .filter { it.size == 2 }
            .compose(clickDebounce())
            .subscribe { l?.onClick(itemView) }
    }
}
