package com.ringoid.debug

import com.ringoid.debug.model.DebugLogItem
import io.reactivex.Single

interface IDebugLogDaoHelper {

    fun debugLog(): Single<List<DebugLogItem>>
    fun addDebugLog(log: DebugLogItem)
    fun deleteDebugLog()
    fun extractLog(): String
}
