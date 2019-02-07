package com.ringoid.base.adapter

import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.*
import com.ringoid.domain.model.IListModel

abstract class OriginListAdapter<T : IListModel, VH : BaseViewHolder<T>>(diffCb: BaseDiffCallback<T>, private val headerRows: Int = 0)
    : RecyclerView.Adapter<VH>() {

    init {
        setHasStableIds(true)

        if (withHeader() && getHeaderLayoutResId() == 0) {
            throw IllegalArgumentException("Need to supply header layout resource id in subclass for header")
        }
    }

    companion object {
        const val VIEW_TYPE_NORMAL = 0
        const val VIEW_TYPE_HEADER = 1
        const val VIEW_TYPE_FOOTER = 2
        const val VIEW_TYPE_LOADING = 3
        const val VIEW_TYPE_ERROR = 4
    }

    private val helper by lazy {
        AsyncListDiffer<T>(
            ExposedAdapterListUpdateCallback(
                this, headerRows = headerRows, exposedCb = { getExposedCb()?.invoke() },
                onInsertedCb = getOnInsertedCb(), onRemovedCb = getOnRemovedCb(),
                onMovedCb = getOnMovedCb(), onChangedCb = getOnChangedCb()),
            AsyncDifferConfig.Builder(diffCb).build())
    }

    // --------------------------------------------------------------------------------------------
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: List<Any>) {
        payloads
            .takeIf { !it.isEmpty() }
            ?.let { holder.bind(getItem(position), payloads) }
            ?: run { holder.bind(getItem(position)) }
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

    protected open fun getOnInsertedCb(): ((position: Int, count: Int) -> Unit)? = null
    protected open fun getOnRemovedCb(): ((position: Int, count: Int) -> Unit)? = null
    protected open fun getOnMovedCb(): ((fromPosition: Int, toPosition: Int) -> Unit)? = null
    protected open fun getOnChangedCb(): ((position: Int, count: Int) -> Unit)? = null

    // --------------------------------------------------------------------------------------------
    var itemClickListener: ((model: T, position: Int) -> Unit)? = null

    protected open fun getOnItemClickListener(vh: VH) = wrapOnItemClickListener(vh, itemClickListener)
    protected open fun wrapOnItemClickListener(vh: VH, l: ((model: T, position: Int) -> Unit)?) =
        View.OnClickListener { vh.adapterPosition.takeIf { it != RecyclerView.NO_POSITION }?.let { l?.invoke(getItem(it), it) } }

    /* Data Access */
    // --------------------------------------------------------------------------------------------
    private var isInErrorState: Boolean = false  // indicates whether should show error item as footer
    private var isThereMore: Boolean = false  // indicates is there more items for paging

    fun clear() {
        isInErrorState = false
        isThereMore = false
        helper.submitList(null)
        notifyDataSetChanged()  // fix possible 'inconsistency detected' error
    }

    fun error() {
        isInErrorState = true
        noMoreItems(emptyList()) { false }
    }

    fun append(list: List<T>?, isThereMore: List<T>.() -> Boolean = { false }) {
        if (!list.isNullOrEmpty()) {
            submitList(mutableListOf<T>().apply { addAll(helper.currentList) }.also { it.addAll(list) })
        }
        noMoreItems(list, isThereMore)
    }

    fun prepend(item: T) {
        submitList(mutableListOf<T>().apply { add(item) }.also { it.addAll(helper.currentList) })
    }

    fun remove(predicate: (item: T) -> Boolean) {
        submitList(ArrayList(helper.currentList).apply { removeAll(predicate) })
    }

    fun submitList(list: List<T>?) {
        isInErrorState = false
        if (list.isNullOrEmpty()) {
            clear()
        } else {
            helper.submitList(list)
        }
        onSubmitList(list)
    }

    fun submitList(list: List<T>?, isThereMore: List<T>.() -> Boolean = { false }) {
        submitList(list)
        noMoreItems(list, isThereMore)
    }

    protected open fun onSubmitList(list: List<T>?) {
        // override in subclasses
    }

    private fun noMoreItems(list: List<T>?, isThereMore: List<T>.() -> Boolean = { false }) {
        val previous = this.isThereMore  // old value
        this.isThereMore = list?.isThereMore() == true
        if (previous != this.isThereMore) {
            notifyItemChanged(footerPosition())
        }
    }

    // ------------------------------------------
    override fun getItemId(position: Int): Long {
        val viewType = getItemViewType(position)
        return when (viewType) {
            VIEW_TYPE_NORMAL -> getModel(position).getModelId()
            /**
             * Any of: VIEW_TYPE_HEADER, VIEW_TYPE_FOOTER, VIEW_TYPE_LOADING, VIEW_TYPE_ERROR
             */
            else -> viewType.toLong()
        }
    }

    protected open fun getItem(position: Int): T =
        when (getItemViewType(position)) {
            VIEW_TYPE_HEADER, VIEW_TYPE_FOOTER, VIEW_TYPE_ERROR, VIEW_TYPE_LOADING -> getStubItem()
            else /* VIEW_TYPE_NORMAL */ -> getModel(position)
        }

    fun getModel(position: Int): T = helper.currentList[position - fixUpForHeader()]
    protected fun getModels(): List<T> = helper.currentList
    /**
     * The following two methods are just stubs to make [getItem] work properly,
     * header and footer [BaseViewHolder]s and corresponding models are normally
     * not used anywhere.
     */
    protected abstract fun getStubItem(): T

    // ------------------------------------------
    fun getItemExposed(position: Int): T = getItem(position)
    fun getItemsExposed(from: Int, to: Int): List<T> {
        if (isEmpty()) {
            return emptyList()
        }

        val list = mutableListOf<T>()
        for (i in from..to) list.add(getItem(i))
        return list
    }

    // ------------------------------------------
    override fun getItemCount(): Int = getModelsCount() + fixUpForHeader() + fixUpForFooter() + fixUpForLoader() + fixUpForError()

    fun getModelsCount(): Int = helper.currentList.size

    override fun getItemViewType(position: Int): Int {
        val footerPosition = footerPosition()
        return if (withHeader() && position == 0) VIEW_TYPE_HEADER
        else if (withLoader() && position == footerPosition) VIEW_TYPE_LOADING
        else if (withError()  && position == footerPosition) VIEW_TYPE_ERROR
        else if (withFooter() && position == footerPosition) VIEW_TYPE_FOOTER
        else VIEW_TYPE_NORMAL
    }

    fun isEmpty(): Boolean = helper.currentList.isEmpty()  // don't count header / footer

    // ------------------------------------------
    @LayoutRes protected open fun getHeaderLayoutResId(): Int = 0
    @LayoutRes protected open fun getFooterLayoutResId(): Int = 0

    fun withHeader(): Boolean = headerRows > 0
    fun withFooter(): Boolean = getFooterLayoutResId() != 0 && !withLoader() && !withError()
    fun withLoader(): Boolean = isThereMore
    fun withError():  Boolean = isInErrorState

    private fun fixUpForHeader(): Int = if (isEmpty()) 0 else if (withHeader()) 1 else 0
    private fun fixUpForFooter(): Int = if (isEmpty()) 0 else if (withFooter()) 1 else 0
    private fun fixUpForLoader(): Int = if (isEmpty()) 0 else if (withLoader()) 1 else 0
    private fun fixUpForError():  Int = if (isEmpty()) 0 else if (withError())  1 else 0
    fun footerPosition(): Int = getModelsCount() + fixUpForHeader()

    // --------------------------------------------------------------------------------------------
    override fun toString(): String = "$javaClass: size=${getModelsCount()}, withHeader=${withHeader()}, withFooter=${withFooter()}[at ${footerPosition()}]"
}

// ------------------------------------------------------------------------------------------------
abstract class BaseDiffCallback<T> : DiffUtil.ItemCallback<T>()
