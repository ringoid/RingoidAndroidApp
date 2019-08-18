package com.ringoid.domain.action_storage

import com.ringoid.domain.model.actions.OriginActionObject
import io.reactivex.Single

interface IActionObjectPool {

    fun countActionObjects(): Single<Int>

    fun put(aobj: OriginActionObject, onComplete: (() -> Unit)? = null)
    fun put(aobjs: Collection<OriginActionObject>, onComplete: (() -> Unit)? = null)

    fun commitNow(aobj: OriginActionObject): Single<Long>
    fun trigger()
    fun triggerSource(): Single<Long>

    fun lastActionTime(): Long
    fun finalizePool()
}
