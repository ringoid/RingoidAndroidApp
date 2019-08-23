package com.ringoid.base.adapter

import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.ListUpdateCallback

class SimpleListDiffer<T>(private val cb: ListUpdateCallback, diffCb: BaseDiffCallback<T>)
    : AsyncListDiffer<T>(cb, AsyncDifferConfig.Builder(diffCb).build()) {

    private val currentList: MutableList<T> = mutableListOf()

    override fun getCurrentList(): MutableList<T> = currentList

    override fun submitList(list: List<T>?) {
        val countRemoved = currentList.size
        currentList.clear()
        cb.onRemoved(0, countRemoved)

        if (list.isNullOrEmpty()) {
            return
        }

        currentList.addAll(list)
        cb.onInserted(0, list.size)
    }
}
