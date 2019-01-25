package com.ringoid.widget.decor

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class BottomDividerItemDecoration(private val marginInDp: Int) : RecyclerView.ItemDecoration() {

    constructor(context: Context, marginResId: Int)
        : this(marginInDp = context.resources.getDimensionPixelSize(marginResId))

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = marginInDp
    }
}
