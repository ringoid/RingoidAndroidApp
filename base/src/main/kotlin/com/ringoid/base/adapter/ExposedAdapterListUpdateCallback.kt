package com.ringoid.base.adapter

import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView

class ExposedAdapterListUpdateCallback(
    private val adapter: RecyclerView.Adapter<*>, private val exposedCb: () -> Unit)
    : ListUpdateCallback {

    override fun onInserted(position: Int, count: Int) {
        adapter.notifyItemRangeInserted(position, count)
        exposedCb.invoke()
    }

    override fun onRemoved(position: Int, count: Int) {
        adapter.notifyItemRangeRemoved(position, count)
        exposedCb.invoke()
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        adapter.notifyItemMoved(fromPosition, toPosition)
        exposedCb.invoke()
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        adapter.notifyItemRangeChanged(position, count, payload)
        exposedCb.invoke()
    }
}
