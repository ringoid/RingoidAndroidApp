package com.ringoid.origin.feed.adapter.base

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

class FeedItemAnimator(l: ((position: Int) -> Unit)? = null) : DefaultItemAnimator() {

    private var removed = false

    var onItemRemoved: ((position: Int) -> Unit)? = l

    override fun onRemoveFinished(item: RecyclerView.ViewHolder) {
        super.onRemoveFinished(item)
        removed = true
        Timber.w("ANIM: ${item.adapterPosition}")
    }

    override fun onMoveFinished(item: RecyclerView.ViewHolder) {
        super.onMoveFinished(item)
        if (removed) {
            removed = false
            onItemRemoved?.invoke(item.adapterPosition)
            Timber.e("ANIMA: ${item.adapterPosition}")
        }
    }
}
