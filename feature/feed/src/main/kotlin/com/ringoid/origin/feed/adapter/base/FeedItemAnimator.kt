package com.ringoid.origin.feed.adapter.base

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

open class FeedItemAnimator : DefaultItemAnimator() {

    private var removed = false

    private val removeAnimationSubject = PublishSubject.create<Int>()
    internal fun removeAnimationFinishedSource(): Observable<Int> = removeAnimationSubject.hide()

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
