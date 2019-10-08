package com.ringoid.datainterface.local.action_storage

import com.ringoid.domain.model.actions.OriginActionObject
import io.reactivex.Single

interface IActionObjectDbFacade {

    fun actionObjects(): Single<List<OriginActionObject>>

    fun actionObjectsMarkAsUsed(): Single<List<OriginActionObject>>

    fun addActionObject(aobj: OriginActionObject)

    fun addActionObjects(objects: Collection<OriginActionObject>)

    fun countActionObjects(): Single<Int>

    fun deleteActionObjects()

    fun deleteActionObjectsForType(type: String)

    fun deleteUsedActionObjects()

    fun unmarkUsedActionObjects(objects: Collection<OriginActionObject>)
}
