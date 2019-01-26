package com.ringoid.base.adapter

import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.*
import com.ringoid.domain.model.IListModel

abstract class OriginListAdapter<T : IListModel, VH : BaseViewHolder<T>>(diffCb: BaseDiffCallback<T>)
    : RecyclerView.Adapter<VH>() {

    init {
        setHasStableIds(true)
    }

    companion object {
        const val VIEW_TYPE_NORMAL = 0
        const val VIEW_TYPE_HEADER = 1
        const val VIEW_TYPE_LOADING = 2
        const val VIEW_TYPE_FEED_END = 3
    }

    private val helper = AsyncListDiffer<T>(
        ExposedAdapterListUpdateCallback(this, exposedCb = { getExposedCb()?.invoke() }),
        AsyncDifferConfig.Builder(diffCb).build())

    // --------------------------------------------------------------------------------------------
    override fun onBindViewHolder(holder: VH, position: Int) {
        onBindViewHolder(holder, position, payloads = emptyList())
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: List<Any>) {
        holder.bind(getItem(position), payloads)
    }

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

    // --------------------------------------------------------------------------------------------
    var itemClickListener: ((model: T, position: Int) -> Unit)? = null

    protected open fun getOnItemClickListener(vh: VH) = wrapOnItemClickListener(vh, itemClickListener)
    protected open fun wrapOnItemClickListener(vh: VH, l: ((model: T, position: Int) -> Unit)?) =
        View.OnClickListener { vh.adapterPosition.takeIf { it != RecyclerView.NO_POSITION }?.let { l?.invoke(getItem(it), it) } }

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
    override fun getItemId(position: Int): Long = getItem(position).getModelId()

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

    fun isEmpty(): Boolean = helper.currentList.isEmpty()  // don't count header / footer

    // ------------------------------------------
    @LayoutRes protected open fun getHeaderLayoutResId(): Int = 0

    private fun withHeader(): Boolean = getHeaderLayoutResId() != 0
}

// ------------------------------------------------------------------------------------------------
abstract class BaseDiffCallback<T> : DiffUtil.ItemCallback<T>()
