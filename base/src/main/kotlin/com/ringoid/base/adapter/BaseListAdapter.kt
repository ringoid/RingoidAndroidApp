package com.ringoid.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class BaseListAdapter<T, VH : BaseViewHolder<T>>(diffCb: BaseDiffCallback<T>)
    : OriginListAdapter<T, VH>(diffCb) {

    @LayoutRes protected abstract fun getLayoutId(): Int

    protected abstract fun instantiateViewHolder(view: View): VH
    protected abstract fun instantiateHeaderViewHolder(view: View): VH

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layoutResId = when (viewType) {
            VIEW_TYPE_HEADER -> getHeaderLayoutResId()
            else -> getLayoutId()
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)

        return when (viewType) {
            VIEW_TYPE_HEADER -> instantiateHeaderViewHolder(view)
            else -> instantiateViewHolder(view).apply { setOnClickListener(getOnItemClickListener(this)) }
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        onBindViewHolder(holder, position, payloads = emptyList())
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: List<Any>) {
        holder.bind(getItem(position), payloads)
    }

    // --------------------------------------------------------------------------------------------
    var itemClickListener: ((model: T, position: Int) -> Unit)? = null

    protected open fun getOnItemClickListener(vh: VH) = wrapOnItemClickListener(vh, itemClickListener)
    protected open fun wrapOnItemClickListener(vh: VH, l: ((model: T, position: Int) -> Unit)?) =
        View.OnClickListener { vh.adapterPosition.takeIf { it != RecyclerView.NO_POSITION }?.let { l?.invoke(getItem(it), it) } }
}
