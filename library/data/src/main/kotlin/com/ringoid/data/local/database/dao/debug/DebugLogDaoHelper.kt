package com.ringoid.data.local.database.dao.debug

import com.ringoid.debug.model.DebugLogItem
import com.ringoid.debug.IDebugLogDaoHelper
import com.ringoid.domain.model.mapList
import com.ringoid.utility.DebugOnly
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton @DebugOnly
class DebugLogDaoHelper @Inject constructor(private val dao: DebugLogDao) : IDebugLogDaoHelper {

    override fun debugLog(): Single<List<DebugLogItem>> = dao.debugLog().map { it.mapList() }

    private val inMemoryLogs = ArrayList<String>(2000)

    @Suppress("CheckResult")
    override fun addDebugLog(log: DebugLogItem) {
        inMemoryLogs.add(log.log())
//        Completable.fromCallable { dao.addDebugLog(DebugLogItemDbo.from(log)) }
//            .subscribeOn(Schedulers.io())
//            .subscribe({}, Timber::e)
    }

    @Suppress("CheckResult")
    override fun deleteDebugLog() {
        inMemoryLogs.clear()
//        Completable.fromCallable { dao.deleteDebugLog() }
//            .subscribeOn(Schedulers.io())
//            .subscribe({}, Timber::e)
    }

    override fun extractLog(): String = inMemoryLogs.joinToString("\n")
}
