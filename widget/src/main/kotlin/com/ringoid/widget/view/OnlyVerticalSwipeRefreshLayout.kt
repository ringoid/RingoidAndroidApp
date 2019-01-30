package com.ringoid.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ringoid.utility.checkMainThread2
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

interface OnVerticalSwipeListener {

    fun onVerticalSwipe()
}

class OnlyVerticalSwipeRefreshLayout(context: Context, attrs: AttributeSet) : SwipeRefreshLayout(context, attrs) {

    private val touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    private var prev: MotionEvent? = null
    private var declined: Boolean = false

    private var listener: OnVerticalSwipeListener? = null

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                prev = MotionEvent.obtain(event)
                declined = false  // new action
            }
            MotionEvent.ACTION_MOVE -> {
                listener?.onVerticalSwipe()
                val xDiff = Math.abs(event.x - (prev?.x ?: 0f))
                if (declined || xDiff > touchSlop) {
                    declined = true  // memorize
                    return false
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                prev?.recycle()
                prev = null
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    /* Listener */
    // --------------------------------------------------------------------------------------------
    fun setOnVerticalSwipeListener(l: OnVerticalSwipeListener?) {
        listener = l
    }
}

fun OnlyVerticalSwipeRefreshLayout.swipes(): Observable<Unit> =
    OnlyVerticalSwipeRefreshLayoutRefreshObservable(this)

class OnlyVerticalSwipeRefreshLayoutRefreshObservable(val view: OnlyVerticalSwipeRefreshLayout) : Observable<Unit>() {

    override fun subscribeActual(observer: Observer<in Unit>) {
        if (!checkMainThread2(observer)) {
            return
        }
        val listener = Listener(view, observer)
        observer.onSubscribe(listener)
        view.setOnVerticalSwipeListener(listener)
    }

    private class Listener(private val view: OnlyVerticalSwipeRefreshLayout,
                           private val observer: Observer<in Unit>)
        : MainThreadDisposable(), OnVerticalSwipeListener {

        override fun onVerticalSwipe() {
            if (!isDisposed) {
                observer.onNext(Unit)
            }
        }

        override fun onDispose() {
            view.setOnVerticalSwipeListener(null)
        }
    }
}
