package com.ringoid.origin.view.debug

import android.view.View
import com.ringoid.base.adapter.SimpleListAdapter
import com.ringoid.origin.R

class SimpleDebugLogItemAdapter(insertCb: ((total: Int) -> Unit)? = null)
    : SimpleListAdapter<DebugLogItemVO, OriginDebugLogItemViewHolder>(insertCb) {

    override fun getLayoutId(): Int = R.layout.rv_item_debug_log

    override fun instantiateViewHolder(view: View) = DebugLogItemViewHolder(view)

    // ------------------------------------------
    fun getContentText(): String = models.joinToString("\n", transform = { it.log.log() })
}
