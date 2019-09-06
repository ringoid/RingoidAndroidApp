package com.ringoid.origin.view.debug

import com.ringoid.base.adapter.BaseDiffCallback

class DebugLogItemDiffCallback : BaseDiffCallback<DebugLogItemVO>() {

    override fun areItemsTheSame(oldItem: DebugLogItemVO, newItem: DebugLogItemVO): Boolean =
        oldItem.log.tick == newItem.log.tick

    override fun areContentsTheSame(oldItem: DebugLogItemVO, newItem: DebugLogItemVO): Boolean =
        oldItem.log.tick == newItem.log.tick
}
