package com.ringoid.origin.feed.adapter.base

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class FeedItemAnimator(l: ((position: Int) -> Unit)? = null) : DefaultItemAnimator() {

    private var removed = false

    internal val removeAnimationSubject = PublishSubject.create<Int>()

    init {
        removeAnimationSubject.subscribe({ l?.invoke(it) }, Timber::e)
    }

    override fun onRemoveFinished(item: RecyclerView.ViewHolder) {
        super.onRemoveFinished(item)
        removed = true
    }

    override fun onMoveFinished(item: RecyclerView.ViewHolder) {
        super.onMoveFinished(item)
        if (removed) {
            removed = false
            removeAnimationSubject.onNext(item.adapterPosition)
        }
    }
}
