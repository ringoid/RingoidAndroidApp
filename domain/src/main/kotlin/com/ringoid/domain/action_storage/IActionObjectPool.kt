package com.ringoid.domain.action_storage

import com.ringoid.domain.model.actions.ActionObject
import io.reactivex.Single

interface IActionObjectPool {

    fun put(aobj: ActionObject)

    fun trigger()
    fun triggerSource(): Single<Long>

    fun finalizePool()
}
