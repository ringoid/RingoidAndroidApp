package com.ringoid.base.adapter

import androidx.recyclerview.widget.*

abstract class OriginListAdapter<T, VH : BaseViewHolder<T>>(diffCb: BaseDiffCallback<T>)
    : RecyclerView.Adapter<VH>() {

    private val helper = AsyncListDiffer<T>(AdapterListUpdateCallback(this), AsyncDifferConfig.Builder(diffCb).build())

    /* Data Access */
    // --------------------------------------------------------------------------------------------
    fun prepend(item: T) {
        helper.submitList(mutableListOf<T>().apply { add(item) }.also { it.addAll(helper.currentList) })
    }

    fun remove(predicate: (item: T) -> Boolean) {
        helper.submitList(ArrayList(helper.currentList).apply { removeAll(predicate) })
    }

    open fun submitList(list: List<T>?) {
        helper.submitList(list)
    }

    // ------------------------------------------
    protected fun getItem(position: Int): T = helper.currentList[position]
    protected fun getItems() = helper.currentList

    override fun getItemCount(): Int = helper.currentList.size
}

// ------------------------------------------------------------------------------------------------
abstract class BaseDiffCallback<T> : DiffUtil.ItemCallback<T>()
