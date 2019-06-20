package com.ringoid.domain.action_storage

import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.model.actions.OriginActionObject
import io.reactivex.Completable
import io.reactivex.Single

interface IActionObjectPool {

    fun put(aobj: OriginActionObject)
    fun put(aobjs: Collection<OriginActionObject>)

    @DebugOnly fun putSource(aobj: OriginActionObject): Completable
    @DebugOnly fun putSource(aobjs: Collection<OriginActionObject>): Completable

    fun trigger()
    fun triggerSource(): Single<Long>

    fun lastActionTime(): Long
    fun finalizePool()
}
