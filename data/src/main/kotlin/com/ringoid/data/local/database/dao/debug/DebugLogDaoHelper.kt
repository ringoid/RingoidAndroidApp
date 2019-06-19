package com.ringoid.data.local.database.dao.debug

import com.ringoid.domain.debug.DebugLogItem
import com.ringoid.domain.debug.IDebugLogDaoHelper
import com.ringoid.domain.model.mapList
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugLogDaoHelper @Inject constructor(private val dao: DebugLogDao) : IDebugLogDaoHelper {

    override fun debugLog(): Single<List<DebugLogItem>> = dao.debugLog().map { it.mapList() }

    @Suppress("CheckResult")
    override fun addDebugLog(log: DebugLogItem) {
//        Completable.fromCallable { dao.addDebugLog(DebugLogItemDbo.from(log)) }
//            .subscribeOn(Schedulers.io())
//            .subscribe({}, Timber::e)
    }

    @Suppress("CheckResult")
    override fun deleteDebugLog() {
//        Completable.fromCallable { dao.deleteDebugLog() }
//            .subscribeOn(Schedulers.io())
//            .subscribe({}, Timber::e)
    }
}
