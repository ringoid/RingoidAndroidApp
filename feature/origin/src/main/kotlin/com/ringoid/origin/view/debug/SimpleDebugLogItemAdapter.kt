package com.ringoid.origin.view.debug

import android.view.View
import com.ringoid.base.adapter.SimpleListAdapter
import com.ringoid.origin.R

class SimpleDebugLogItemAdapter(insertCb: ((total: Int) -> Unit)? = null)
    : SimpleListAdapter<DebugLogItemVO, OriginDebugLogItemViewHolder>(insertCb) {

    companion object {
        const val BUFFER_LIMIT = 60
    }

    private val buffer = ArrayList<DebugLogItemVO>(BUFFER_LIMIT + 2)

    override fun getLayoutId(): Int = R.layout.rv_item_debug_log

    override fun instantiateViewHolder(view: View) = DebugLogItemViewHolder(view)

    // ------------------------------------------
    fun getContentText(): String = models.joinToString("\n", transform = { it.log.log() })

    override fun append(item: DebugLogItemVO) {
        buffer.add(item)
        if (buffer.size >= BUFFER_LIMIT) {
            val position = models.size
            buffer.sortBy { it.log.tick }
            models.addAll(buffer)
            buffer.clear()
            notifyItemRangeInserted(position, buffer.size)
            insertCb?.invoke(models.size)  // total size
        }
    }
}
