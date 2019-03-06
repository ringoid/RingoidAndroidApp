package com.ringoid.origin.feed.adapter.base

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class FeedItemAnimator(l: ((position: Int) -> Unit)? = null) : DefaultItemAnimator() {

    private var removed = false

    var onItemRemoved: ((position: Int) -> Unit)? = l

    override fun onRemoveFinished(item: RecyclerView.ViewHolder) {
        super.onRemoveFinished(item)
        removed = true
    }

    override fun onMoveFinished(item: RecyclerView.ViewHolder) {
        super.onMoveFinished(item)
        if (removed) {
            removed = false
            onItemRemoved?.invoke(item.adapterPosition)
        }
    }
}
