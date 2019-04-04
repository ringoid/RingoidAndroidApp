package com.ringoid.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.IListModel

abstract class SimpleListAdapter<T : IListModel, VH : BaseViewHolder<T>>(
    private val insertCb: ((total: Int) -> Unit)? = null) : RecyclerView.Adapter<VH>() {

    init {
        setHasStableIds(true)
    }

    protected val models = mutableListOf<T>()

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
        val position = models.size
        models.add(item)
        notifyItemInserted(position)
        insertCb?.invoke(models.size)  /* total count */
    }

    fun clear() {
        models.clear()
        notifyDataSetChanged()
    }
}
