package com.ringoid.origin.view.debug

import android.view.View
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.origin.R

class DebugLogItemAdapter : BaseListAdapter<DebugLogItemVO, OriginDebugLogItemViewHolder>(DebugLogItemDiffCallback()) {

    override fun getLayoutId(): Int = R.layout.rv_item_debug_log

    override fun instantiateViewHolder(view: View) = DebugLogItemViewHolder(view)

    override fun instantiateHeaderViewHolder(view: View) = HeaderDebugLogItemViewHolder(view)
    override fun instantiateFooterViewHolder(view: View) = FooterDebugLogItemViewHolder(view)

    // ------------------------------------------
    override fun getStubItem(): DebugLogItemVO = EmptyDebugLogItemVO

    // ------------------------------------------
    fun getContentText(): String = helper.currentList.joinToString("\n", transform = { it.log.log() })
}
