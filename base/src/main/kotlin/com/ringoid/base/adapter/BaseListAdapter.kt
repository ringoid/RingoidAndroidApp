package com.ringoid.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseListAdapter<T, VH : BaseViewHolder<T>>(diffCb: BaseDiffCallback<T>)
    : ListAdapter<T, VH>(diffCb) {

    @LayoutRes protected abstract fun getLayoutId(): Int

    protected abstract fun instantiateViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null): VH

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        LayoutInflater.from(parent.context).inflate(getLayoutId(), parent, false)
            .let { instantiateViewHolder(it) }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}

// ------------------------------------------------------------------------------------------------
abstract class BaseDiffCallback<T> : DiffUtil.ItemCallback<T>()
