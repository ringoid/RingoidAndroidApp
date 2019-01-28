package com.ringoid.base.adapter

import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView

class ExposedAdapterListUpdateCallback(
    private val adapter: RecyclerView.Adapter<*>,
    private val headerRows: Int = 0, private val exposedCb: () -> Unit)
    : ListUpdateCallback {

    override fun onInserted(position: Int, count: Int) {
        adapter.notifyItemRangeInserted(offsetPosition(position), count)
        exposedCb.invoke()
    }

    override fun onRemoved(position: Int, count: Int) {
        adapter.notifyItemRangeRemoved(offsetPosition(position), count)
        exposedCb.invoke()
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        adapter.notifyItemMoved(offsetPosition(fromPosition), offsetPosition(toPosition))
        exposedCb.invoke()
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        adapter.notifyItemRangeChanged(offsetPosition(position), count, payload)
        exposedCb.invoke()
    }

    private fun offsetPosition(position: Int): Int = position + headerRows
}
