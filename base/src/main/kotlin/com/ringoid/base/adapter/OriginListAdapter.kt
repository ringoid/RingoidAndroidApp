package com.ringoid.base.adapter

import androidx.recyclerview.widget.*

abstract class OriginListAdapter<T, VH : BaseViewHolder<T>>(diffCb: BaseDiffCallback<T>)
    : RecyclerView.Adapter<VH>() {

    private val helper = AsyncListDiffer<T>(AdapterListUpdateCallback(this), AsyncDifferConfig.Builder(diffCb).build())

    /* Data Access */
    // --------------------------------------------------------------------------------------------
    open fun submitList(list: List<T>?) {
        helper.submitList(list)
    }

    fun remove(predicate: (item: T) -> Boolean) {
        helper.submitList(ArrayList(helper.currentList).apply { removeAll(predicate) })
    }

    // ------------------------------------------
    protected fun getItem(position: Int): T = helper.currentList[position]

    override fun getItemCount(): Int = helper.currentList.size
}

// ------------------------------------------------------------------------------------------------
abstract class BaseDiffCallback<T> : DiffUtil.ItemCallback<T>()
