package com.ringoid.base.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.utility.clickDebounce

abstract class BaseViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {

    abstract fun bind(model: T)

    @Suppress("CheckResult")
    open fun setOnClickListener(l: View.OnClickListener?) {
        itemView.apply {
            isClickable = l != null
            clicks().compose(clickDebounce()).subscribe { l?.onClick(this) }
        }
    }
}
