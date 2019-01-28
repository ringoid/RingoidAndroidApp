package com.ringoid.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.ringoid.domain.model.IListModel

abstract class BaseListAdapter<T : IListModel, VH : BaseViewHolder<T>>(diffCb: BaseDiffCallback<T>)
    : OriginListAdapter<T, VH>(diffCb) {

    @LayoutRes protected abstract fun getLayoutId(): Int

    protected abstract fun instantiateViewHolder(view: View): VH
    protected abstract fun instantiateHeaderViewHolder(view: View): VH

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layoutResId = when (viewType) {
            VIEW_TYPE_HEADER -> getHeaderLayoutResId()
            VIEW_TYPE_FOOTER -> getFooterLayoutResId()
            else -> getLayoutId()
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)

        return when (viewType) {
            VIEW_TYPE_HEADER -> instantiateHeaderViewHolder(view)
            else -> instantiateViewHolder(view).apply { setOnClickListener(getOnItemClickListener(this)) }
        }
    }
}
