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
import timber.log.Timber
import java.util.concurrent.TimeUnit

interface OnVerticalSwipeListener {

    fun onVerticalSwipe()
}

class OnlyVerticalSwipeRefreshLayout(context: Context, attrs: AttributeSet) : SwipeRefreshLayout(context, attrs) {

    private val touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    private var prevX: Float = 0.0f
    private var declined: Boolean = false
    private var hasDraggingStarted: Boolean = false

    private var listener: OnVerticalSwipeListener? = null

    @Suppress("CheckResult")
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                prevX = event.x
                declined = false  // new action
            }
            MotionEvent.ACTION_MOVE -> {
                if (declined || Math.abs(event.x - prevX) > touchSlop) {
                    declined = true  // memorize
                    hasDraggingStarted = false
                    return false
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                prevX = event.x
                hasDraggingStarted = false
            }
        }
        val result = super.onInterceptTouchEvent(event)
        if (result && event.action == MotionEvent.ACTION_MOVE) {
            hasDraggingStarted = true
            Observable.timer(300L, TimeUnit.MILLISECONDS)
                .subscribe({
                    if (hasDraggingStarted) {
                        listener?.onVerticalSwipe()
                    }
                }, Timber::e)
        }
        return result
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
