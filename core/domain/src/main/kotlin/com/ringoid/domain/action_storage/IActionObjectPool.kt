package com.ringoid.domain.action_storage

import com.ringoid.domain.model.actions.OriginActionObject
import io.reactivex.Single

interface IActionObjectPool {

    fun put(aobj: OriginActionObject)
    fun put(aobjs: Collection<OriginActionObject>)

    fun trigger()
    fun triggerSource(): Single<Long>

    fun lastActionTime(): Long
    fun finalizePool()
}
