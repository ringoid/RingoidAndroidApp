package com.ringoid.data.local.database.facade.action_storage

import com.ringoid.data.local.database.dao.action_storage.ActionObjectDao
import com.ringoid.data.local.database.model.action_storage.ActionObjectDboMapper
import com.ringoid.datainterface.local.action_storage.IActionObjectDbFacade
import com.ringoid.domain.model.actions.OriginActionObject
import com.ringoid.domain.model.mapList
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionObjectDbFacadeImpl @Inject constructor(
    private val dao: ActionObjectDao,
    private val mapper: ActionObjectDboMapper) : IActionObjectDbFacade {

    override fun actionObjects(): Single<List<OriginActionObject>> =
        dao.actionObjects().map { it.mapList() }

    override fun actionObjectsMarkAsUsed(): Single<List<OriginActionObject>> =
        dao.actionObjects()
            .flatMap { objects ->
                Completable.fromCallable { dao.markActionObjectsAsUsed(ids = objects.map { it.id }) }
                           .toSingleDefault(objects)
                           .map { it.mapList() }
            }

    override fun addActionObject(aobj: OriginActionObject) {
        mapper.map(aobj).also { dao.addActionObject(it) }
    }

    override fun addActionObjects(objects: Collection<OriginActionObject>) {
        objects.map(mapper::map).also { dao.addActionObjects(it) }
    }

    override fun countActionObjects(): Single<Int> = dao.countActionObjects()

    override fun deleteActionObjects() {
        dao.deleteActionObjects()
    }

    override fun deleteUsedActionObjects() {
        dao.deleteUsedActionObjects()
    }
}
