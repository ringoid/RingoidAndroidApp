package com.ringoid.origin.view.debug

import com.ringoid.debug.model.DebugLogItem
import com.ringoid.debug.model.EmptyDebugLogItem
import com.ringoid.domain.model.IListModel

data class DebugLogItemVO(val log: DebugLogItem) : IListModel {

    override fun getModelId(): Long = log.tick

    companion object {
        fun from(log: DebugLogItem): DebugLogItemVO = DebugLogItemVO(log)
    }
}

val EmptyDebugLogItemVO = DebugLogItemVO(log = EmptyDebugLogItem)
