package com.ringoid.debug

import io.reactivex.Single

interface IBarrierLogDaoHelper {

    fun log(): Single<List<BarrierLogItem>>
    fun addLogs(logs: List<BarrierLogItem>)
    fun deleteLog()
}
