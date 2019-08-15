package com.ringoid.data.local.database.dao.debug

import com.ringoid.data.local.database.model.debug.BarrierLogItemDbo
import com.ringoid.domain.debug.BarrierLogItem
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.debug.IBarrierLogDaoHelper
import com.ringoid.domain.model.mapList
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton @DebugOnly
class BarrierLogDaoHelper @Inject constructor(private val dao: BarrierLogDao) : IBarrierLogDaoHelper {

    override fun log(): Single<List<BarrierLogItem>> = dao.log().map { it.mapList() }

    @Suppress("CheckResult")
    override fun addLogs(logs: List<BarrierLogItem>) {
        Completable.fromCallable { dao.addLogs(logs.map { BarrierLogItemDbo.from(it) }) }
            .subscribeOn(Schedulers.io())
            .subscribe({}, Timber::e)
    }

    @Suppress("CheckResult")
    override fun deleteLog() {
        Completable.fromCallable { dao.deleteLog() }
            .subscribeOn(Schedulers.io())
            .subscribe({}, Timber::e)
    }
}
