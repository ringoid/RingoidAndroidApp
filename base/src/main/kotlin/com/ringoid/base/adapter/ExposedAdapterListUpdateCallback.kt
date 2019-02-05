package com.ringoid.base.adapter

import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView

class ExposedAdapterListUpdateCallback(
    private val adapter: RecyclerView.Adapter<*>,
    private val headerRows: Int = 0, private val exposedCb: () -> Unit,
    private val onInsertedCb: ((position: Int, count: Int) -> Unit)? = null,
    private val onRemovedCb: ((position: Int, count: Int) -> Unit)? = null,
    private val onMovedCb: ((fromPosition: Int, toPosition: Int) -> Unit)? = null,
    private val onChangedCb: ((position: Int, count: Int) -> Unit)? = null)
    : ListUpdateCallback {

    override fun onInserted(position: Int, count: Int) {
        adapter.notifyItemRangeInserted(offsetPosition(position), count)
        exposedCb.invoke()
        onInsertedCb?.invoke(position, count)
    }

    override fun onRemoved(position: Int, count: Int) {
        adapter.notifyItemRangeRemoved(offsetPosition(position), count)
        exposedCb.invoke()
        onRemovedCb?.invoke(position, count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        adapter.notifyItemMoved(offsetPosition(fromPosition), offsetPosition(toPosition))
        exposedCb.invoke()
        onMovedCb?.invoke(fromPosition, toPosition)
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        adapter.notifyItemRangeChanged(offsetPosition(position), count, payload)
        exposedCb.invoke()
        onChangedCb?.invoke(position, count)
    }

    private fun offsetPosition(position: Int): Int = position + headerRows
}
