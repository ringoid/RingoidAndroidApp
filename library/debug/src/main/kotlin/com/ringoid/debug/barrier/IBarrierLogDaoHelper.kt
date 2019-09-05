package com.ringoid.debug.barrier

import com.ringoid.debug.model.BarrierLogItem
import io.reactivex.Single

interface IBarrierLogDaoHelper {

    fun log(): Single<List<BarrierLogItem>>
    fun addLogs(logs: List<BarrierLogItem>)
    fun deleteLog()
}
