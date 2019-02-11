package com.ringoid.widget.view.rv

import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView

class EnhancedPagerSnapHelper : PagerSnapHelper() {

    private lateinit var rv: RecyclerView

    companion object {
        const val FACTOR = 0.5f
        const val MAX_SCROLL_ON_FLING_DURATION = 100
    }

    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        if (recyclerView == null) {
            return
        }

        rv = recyclerView
        super.attachToRecyclerView(recyclerView)
    }

    override fun createSnapScroller(layoutManager: RecyclerView.LayoutManager?): LinearSmoothScroller? {
        if (layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            return null
        }
        return object : LinearSmoothScroller(rv.context) {
            override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
                val snapDistances = calculateDistanceToFinalSnap(rv.layoutManager!!, targetView)
                val dx = snapDistances!![0]
                val dy = snapDistances[1]
                val time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)))
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator)
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float =
                super.calculateSpeedPerPixel(displayMetrics) * FACTOR

            override fun calculateTimeForScrolling(dx: Int): Int =
                Math.min(MAX_SCROLL_ON_FLING_DURATION, super.calculateTimeForScrolling(dx))
        }
    }
}
