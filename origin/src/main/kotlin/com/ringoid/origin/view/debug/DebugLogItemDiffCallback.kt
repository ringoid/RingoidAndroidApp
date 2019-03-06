package com.ringoid.origin.view.debug

import com.ringoid.base.adapter.BaseDiffCallback

class DebugLogItemDiffCallback : BaseDiffCallback<DebugLogItemVO>() {

    override fun areItemsTheSame(oldItem: DebugLogItemVO, newItem: DebugLogItemVO): Boolean =
        oldItem.log.id == newItem.log.id

    override fun areContentsTheSame(oldItem: DebugLogItemVO, newItem: DebugLogItemVO): Boolean =
        oldItem.log == newItem.log
}
