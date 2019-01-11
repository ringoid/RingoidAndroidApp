package com.ringoid.base.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

abstract class BaseListAdapter<T, VH : BaseViewHolder<T>>(diffCb: DiffUtil.ItemCallback<T>)
    : ListAdapter<T, VH>(diffCb) {

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}
