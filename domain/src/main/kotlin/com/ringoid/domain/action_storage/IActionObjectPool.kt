package com.ringoid.domain.action_storage

import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.model.actions.OriginActionObject
import io.reactivex.Single

interface IActionObjectPool {

    fun put(aobj: OriginActionObject)

    fun trigger()
    fun triggerSource(): Single<Long>

    @DebugOnly fun errorTriggerSource(): Single<Long>
    @DebugOnly fun triggerAndDisposeImmediately()

    fun lastActionTime(): Long
    fun finalizePool()
}
