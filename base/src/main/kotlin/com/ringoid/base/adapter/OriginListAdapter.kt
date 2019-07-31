package com.ringoid.base.adapter

import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.*
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.IListModel
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

abstract class OriginListAdapter<T : IListModel, VH : BaseViewHolder<T>>(
    protected val diffCb: BaseDiffCallback<T>, private val headerRows: Int = 0, withListeners: Boolean = true)
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

    protected open val helper by lazy {
        AsyncListDiffer<T>(getAdapterListUpdateCallback(), AsyncDifferConfig.Builder(diffCb).build())
    }

    protected fun getAdapterListUpdateCallback(): ListUpdateCallback =
        ExposedAdapterListUpdateCallback(
            this, headerRows = headerRows, exposedCb = { getExposedCb()?.invoke() },
            insertSubject = insertSubject, removeSubject = removeSubject,
            moveSubject = moveSubject, changeSubject = changeSubject)

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

    val insertSubject = PublishSubject.create<Pair<Int, Int>>()
    val removeSubject = PublishSubject.create<Pair<Int, Int>>()
    val moveSubject = PublishSubject.create<Pair<Int, Int>>()
    val changeSubject = PublishSubject.create<Pair<Int, Int>>()

    private var insertDisposable: Disposable? = null
    private var removeDisposable: Disposable? = null
    private var moveDisposable: Disposable? = null
    private var changeDisposable: Disposable? = null

    init {
        if (withListeners) {
            insertDisposable = insertSubject.subscribe({ getOnInsertedCb()?.invoke(it.first, it.second) }, Timber::e)
            removeDisposable = removeSubject.subscribe({ getOnRemovedCb()?.invoke(it.first, it.second) }, Timber::e)
            moveDisposable = moveSubject.subscribe({ getOnMovedCb()?.invoke(it.first, it.second) }, Timber::e)
            changeDisposable = changeSubject.subscribe({ getOnChangedCb()?.invoke(it.first, it.second) }, Timber::e)
        }
    }

    open fun dispose() {
        insertDisposable?.dispose()
        removeDisposable?.dispose()
        moveDisposable?.dispose()
        changeDisposable?.dispose()
    }

    // --------------------------------------------------------------------------------------------
    var itemClickListener: ((model: T, position: Int) -> Unit)? = null
    var itemDoubleClickListener: ((model: T, position: Int) -> Unit)? = null

    protected open fun getOnItemClickListener(vh: VH) = wrapOnItemClickListener(vh, itemClickListener)
    protected open fun getOnItemDoubleClickListener(vh: VH) = wrapOnItemClickListener(vh, itemDoubleClickListener)
    protected fun wrapOnItemClickListener(vh: VH, l: ((model: T, position: Int) -> Unit)?) =
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

    fun append(item: T) {
        submitList(mutableListOf<T>().apply { addAll(helper.currentList) }.also { it.add(item) })
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

    fun prependAll(items: List<T>) {
        if (items.isEmpty()) {
            return
        }
        submitList(mutableListOf<T>().apply { addAll(items) }.also { it.addAll(helper.currentList) })
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
    }

    fun submitList(list: List<T>?, isThereMore: List<T>.() -> Boolean = { false }) {
        submitList(list)
        noMoreItems(list, isThereMore)
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

    fun applyOnModel(predicate: (item: T) -> Boolean, action: (item: T) -> Unit) {
        findModelAndPosition(predicate)
            ?.let {
                action.invoke(it.second)
                notifyItemChanged(it.first)
            }
    }

    /**
     * Here in all methods that obtain model by position, position is always treated as
     * adapter position, so it must be fixed up against header / footer, if present.
     */
    fun hasModel(position: Int): Boolean = helper.currentList.size > position - fixUpForHeader()
    fun getModel(position: Int): T = helper.currentList[position - fixUpForHeader()]
    protected fun getModels(): List<T> = helper.currentList

    /**
     * Safely access model at [position], or null.
     */
    fun findModel(position: Int): T? =
        if (position < 0 || position >= helper.currentList.size - fixUpForHeader()) null
        else getModel(position)

    fun findModel(predicate: (item: T) -> Boolean): T? = helper.currentList.find(predicate)
    fun findModelAndPosition(predicate: (item: T) -> Boolean): Pair<Int, T>? {
        val position = helper.currentList.indexOfFirst(predicate)
        return if (position > DomainUtil.BAD_POSITION) position to helper.currentList[position]
               else null
    }
    fun findPosition(predicate: (item: T) -> Boolean): Int = helper.currentList.indexOfFirst(predicate)

    fun getModelAdapterPosition(predicate: (item: T) -> Boolean): Int =
        helper.currentList.indexOfFirst(predicate)
            .takeIf { it != DomainUtil.BAD_POSITION }
            ?.let { it + fixUpForHeader() }
            ?: DomainUtil.BAD_POSITION

    fun getModelsInRange(from: Int, to: Int): MutableList<T> {
        if (to < from || from < 0 || to < 0 || to >= itemCount) {
            throw IllegalArgumentException("Invalid input range: ($from, $to) of $itemCount")
        }

        val xfrom = if (withHeader()) from + 1 else from
        val xto = if (getItemViewType(to) != VIEW_TYPE_NORMAL) to - 1 else to

        val list = mutableListOf<T>()
        for (i in xfrom..xto) list.add(getModel(i))
        return list
    }

    /**
     * The following two methods are just stubs to make [getItem] work properly,
     * header and footer [BaseViewHolder]s and corresponding models are normally
     * not used anywhere.
     */
    protected abstract fun getStubItem(): T

    // ------------------------------------------
    fun getItemExposed(position: Int): T = getItem(position)
    fun getItemsExposed(from: Int, to: Int): List<T> {
        if (to < from) {
            throw IllegalArgumentException("Invalid range: [$from, $to]")
        }
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
