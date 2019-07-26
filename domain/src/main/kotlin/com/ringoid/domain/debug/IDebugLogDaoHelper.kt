package com.ringoid.domain.debug

import io.reactivex.Single

interface IDebugLogDaoHelper {

    fun debugLog(): Single<List<DebugLogItem>>
    fun addDebugLog(log: DebugLogItem)
    fun deleteDebugLog()
}
