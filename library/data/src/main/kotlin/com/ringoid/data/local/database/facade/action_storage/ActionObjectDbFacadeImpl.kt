package com.ringoid.data.local.database.facade.action_storage

import com.ringoid.data.local.database.dao.action_storage.ActionObjectDao
import com.ringoid.data.local.database.model.action_storage.ActionObjectDboMapper
import com.ringoid.datainterface.local.action_storage.IActionObjectDbFacade
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.actions.OriginActionObject
import com.ringoid.domain.model.mapList
import com.ringoid.report.log.Report
import com.ringoid.utility.randomInt
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionObjectDbFacadeImpl @Inject constructor(
    private val dao: ActionObjectDao,
    private val mapper: ActionObjectDboMapper) : IActionObjectDbFacade {

    init { init() }

    @Suppress("CheckResult")
    private fun init() {
        // refine legacy db removing invalid action objects, if any
        dao.actionObjects()
            .subscribeOn(Schedulers.io())
            .flatMapObservable { Observable.fromIterable(it) }
            .filter { !it.isValid() }
            .toList()
            .flatMap { Single.fromCallable { dao.deleteActionObjects(it) } }
            .subscribe({
                if (it > 0) {
                    Report.i("Removed invalid aObjs from cache", extras = listOf("size" to "$it"))
                }
            }, Timber::e)

        // assign 'actionId' to action objects that have it missing
        dao.actionObjects()
            .subscribeOn(Schedulers.io())
            .flatMapObservable { Observable.fromIterable(it) }
            .filter { it.actionId == DomainUtil.UNKNOWN_VALUE }
            .map { it.copyWithActionId(actionId = randomInt()) }
            .toList()
            .flatMap { Single.fromCallable { dao.updateActionObjects(it) } }
            .subscribe({
                if (it > 0) {
                    Report.i("Assigned actionIds for aObjs in cache", extras = listOf("size" to "$it"))
                }
            }, Timber::e)
    }

    // ------------------------------------------
    override fun actionObjects(): Single<List<OriginActionObject>> =
        dao.actionObjects().map { it.mapList() }

    override fun actionObjectsMarkAsUsed(): Single<List<OriginActionObject>> =
        Single.fromCallable { dao.markActionObjectsAsUsed() }
            .flatMap { count ->
                if (count > 0) {
                    /**
                     * After marking all available action objects as 'used', we need to access
                     * only these objects. Since insertion to local cache is concurrent, some
                     * new action object could appear in local cache by this moment of time,
                     * when we are retrieving all available action objects, so it is needed
                     * to take up to count of action objects that were actually marked step before.
                     * This relies on sequential order of insertion into local cache.
                     */
                    dao.actionObjects()  // get all currently available action objects
                        .doOnSuccess {
                            if (it.size != count) {
                                // some new action objects have been inserted while this method
                                DebugLogUtil.w("Count of used aobjs [$count] != total [${it.size}]")
                            }
                        }
                       .map { it.take(count).mapList() }  // and take only marked as 'used'
                } else {
                    Single.just<List<OriginActionObject>>(emptyList())
                }
            }

    override fun addActionObject(aobj: OriginActionObject) {
        aobj.takeIf { it.isValid() }?.let { a -> mapper.map(a).also { dao.addActionObject(it) } }
    }

    override fun addActionObjects(objects: Collection<OriginActionObject>) {
        objects.filter { it.isValid() }.map(mapper::map).also { dao.addActionObjects(it) }
    }

    override fun countActionObjects(): Single<Int> = dao.countActionObjects()

    override fun deleteActionObjects() {
        dao.deleteActionObjects()
    }

    override fun deleteActionObjectsForType(type: String) {
        dao.deleteActionObjectsForType(type)
    }

    override fun deleteUsedActionObjects() {
        dao.deleteUsedActionObjects()
    }

    override fun unmarkUsedActionObjects(objects: Collection<OriginActionObject>) {
        dao.markActionObjectsAsUsed(ids = objects.map { it.id }, used = 0)
    }
}
