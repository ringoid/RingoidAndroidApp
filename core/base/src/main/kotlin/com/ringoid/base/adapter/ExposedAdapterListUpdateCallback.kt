package com.ringoid.base.adapter

import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject

class ExposedAdapterListUpdateCallback(
    private val adapter: RecyclerView.Adapter<*>,
    private val headerRows: Int = 0, private val exposedCb: () -> Unit,
    private val insertSubject: PublishSubject<Pair<Int, Int>>,
    private val removeSubject: PublishSubject<Pair<Int, Int>>,
    private val moveSubject: PublishSubject<Pair<Int, Int>>,
    private val changeSubject: PublishSubject<Pair<Int, Int>>)
    : ListUpdateCallback {

    override fun onInserted(position: Int, count: Int) {
        adapter.notifyItemRangeInserted(offsetPosition(position), count)
        exposedCb.invoke()
        insertSubject.onNext(position to count)
    }

    override fun onRemoved(position: Int, count: Int) {
        adapter.notifyItemRangeRemoved(offsetPosition(position), count)
        exposedCb.invoke()
        removeSubject.onNext(position to count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        adapter.notifyItemMoved(offsetPosition(fromPosition), offsetPosition(toPosition))
        exposedCb.invoke()
        moveSubject.onNext(fromPosition to toPosition)
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        adapter.notifyItemRangeChanged(offsetPosition(position), count, payload)
        exposedCb.invoke()
        changeSubject.onNext(position to count)
    }

    private fun offsetPosition(position: Int): Int = position + headerRows
}
