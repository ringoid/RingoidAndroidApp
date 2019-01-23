package com.ringoid.base.adapter

import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.*

abstract class OriginListAdapter<T, VH : BaseViewHolder<T>>(diffCb: BaseDiffCallback<T>)
    : RecyclerView.Adapter<VH>() {

    companion object {
        const val VIEW_TYPE_NORMAL = 0
        const val VIEW_TYPE_HEADER = 1
    }

    private val helper = AsyncListDiffer<T>(
        ExposedAdapterListUpdateCallback(this, exposedCb = { getExposedCb()?.invoke() }),
        AsyncDifferConfig.Builder(diffCb).build())

    /**
     * Exposed callback is called when asynchronous DiffUtil finishes it's computations on background
     * thread and then notify main thread via [ListUpdateCallback]. Due to such callback is assigned
     * to [AsyncListDiffer] in ctor and cannot be reassigned, and it's default implementation
     * [AdapterListUpdateCallback] is final and cannot be overridden, implementation should be
     * copied to [ExposedAdapterListUpdateCallback] and assignable secondary callback must be wrapped
     * into embedded callback [ExposedAdapterListUpdateCallback.exposedCb] passed in ctor. This hence
     * makes it possible to invoke additional callback on finished async DiffUtil computations, and
     * such callback could be properly defined in subclasses of this [OriginListAdapter].
     */
    protected open fun getExposedCb(): (() -> Unit)? = null

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
    protected fun getItem(position: Int): T {
        if (withHeader() && position == 0) {
            return getHeaderItem()
        }
        return helper.currentList[if (withHeader()) position - 1 else position]
    }

    protected fun getItems() = helper.currentList
    protected abstract fun getHeaderItem(): T

    override fun getItemCount(): Int = (if (withHeader()) 1 else 0) + helper.currentList.size
    override fun getItemViewType(position: Int): Int =
        if (withHeader() && position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_NORMAL

    // ------------------------------------------
    @LayoutRes protected open fun getHeaderLayoutResId(): Int = 0

    private fun withHeader(): Boolean = getHeaderLayoutResId() != 0
}

// ------------------------------------------------------------------------------------------------
abstract class BaseDiffCallback<T> : DiffUtil.ItemCallback<T>()
