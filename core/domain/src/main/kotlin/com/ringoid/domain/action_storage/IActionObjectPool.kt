package com.ringoid.domain.action_storage

import com.ringoid.domain.model.actions.OriginActionObject
import io.reactivex.Completable
import io.reactivex.Single

interface IActionObjectPool {

    fun countActionObjects(): Single<Int>

    fun put(aobj: OriginActionObject, onComplete: (() -> Unit)? = null)
    fun put(aobjs: Collection<OriginActionObject>, onComplete: (() -> Unit)? = null)
    fun putSource(aobj: OriginActionObject): Completable
    fun putSource(aobjs: Collection<OriginActionObject>): Completable

    fun deleteActionObjectsForType(type: String): Completable
    fun deleteAllActionObject(): Completable

    fun commitNow(aobj: OriginActionObject): Single<Long>
    fun trigger()
    fun triggerSource(): Single<Long>

    fun isLastActionTimeValid(): Boolean
    fun lastActionTime(): Long
    fun finalizePool()
}
