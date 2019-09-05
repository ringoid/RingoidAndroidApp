package com.ringoid.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.IListModel

abstract class SimpleListAdapter<T : IListModel, VH : BaseViewHolder<T>>(
    private val insertCb: ((total: Int) -> Unit)? = null) : RecyclerView.Adapter<VH>() {

    companion object {
        const val BUFFER_LIMIT = 60
    }

    init {
        setHasStableIds(true)
    }

    protected val models = ArrayList<T>(2000)
    private val buffer = ArrayList<T>(BUFFER_LIMIT + 2)

    @LayoutRes protected abstract fun getLayoutId(): Int
    protected abstract fun instantiateViewHolder(view: View): VH

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(getLayoutId(), parent, false)
        return instantiateViewHolder(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(models[position])
    }

    override fun getItemCount(): Int = models.size

    fun append(item: T) {
        val position = appendOnly(item)
        notifyItemInserted(position)
        insertCb?.invoke(models.size)  /* total count */
    }

    @Synchronized
    fun safeAppend(item: T): Int = appendOnly(item)

    private fun appendOnly(item: T): Int {
        val position = models.size
        models.add(item)
        return position
    }

    fun appendBuffer(item: T) {
        buffer.add(item)
        if (buffer.size >= BUFFER_LIMIT) {
            val position = models.size
            models.addAll(buffer)
            buffer.clear()
//            notifyItemRangeInserted(position, buffer.size)
            notifyDataSetChanged()
            insertCb?.invoke(models.size)
        }
    }

    fun clear() {
        clearOnly()
        notifyDataSetChanged()
    }

    @Synchronized
    fun safeClear() {
        clearOnly()
    }

    private fun clearOnly() {
        models.clear()
    }
}
