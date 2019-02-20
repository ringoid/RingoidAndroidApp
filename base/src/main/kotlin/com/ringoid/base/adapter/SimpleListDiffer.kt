package com.ringoid.base.adapter

import androidx.recyclerview.widget.ListUpdateCallback

class SimpleListDiffer<T>(private val cb: ListUpdateCallback?) {

    internal val currentList: MutableList<T> = mutableListOf()

    fun submitList(list: List<T>?) {
        if (list.isNullOrEmpty()) {
            val countRemoved = currentList.size
            currentList.clear()
            cb?.onRemoved(0, countRemoved)
            return
        }

        currentList.clear()
        currentList.addAll(list)
        cb?.onInserted(0, list.size)
    }
}
