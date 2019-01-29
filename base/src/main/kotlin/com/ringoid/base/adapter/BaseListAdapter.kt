package com.ringoid.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.ringoid.base.R
import com.ringoid.domain.model.IListModel

abstract class BaseListAdapter<T : IListModel, VH : BaseViewHolder<T>>(diffCb: BaseDiffCallback<T>, headerRows: Int = 0)
    : OriginListAdapter<T, VH>(diffCb, headerRows = headerRows) {

    @LayoutRes protected abstract fun getLayoutId(): Int

    protected abstract fun instantiateViewHolder(view: View): VH
    protected abstract fun instantiateHeaderViewHolder(view: View): VH  // default header and footer viewHolders bind nothing
    private fun instantiateFooterViewHolder(view: View): VH = instantiateHeaderViewHolder(view)
    private fun instantiateLoadingViewHolder(view: View): VH = instantiateHeaderViewHolder(view)  // same for loading viewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layoutResId = when (viewType) {
            VIEW_TYPE_HEADER -> getHeaderLayoutResId()
            VIEW_TYPE_FOOTER -> getFooterLayoutResId()
            VIEW_TYPE_LOADING -> R.layout.rv_item_loading
            else -> getLayoutId()
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)

        return when (viewType) {
            VIEW_TYPE_HEADER -> instantiateHeaderViewHolder(view)
            VIEW_TYPE_FOOTER -> instantiateFooterViewHolder(view)
            VIEW_TYPE_LOADING -> instantiateLoadingViewHolder(view)
            else -> instantiateViewHolder(view).apply { setOnClickListener(getOnItemClickListener(this)) }
        }
    }
}
