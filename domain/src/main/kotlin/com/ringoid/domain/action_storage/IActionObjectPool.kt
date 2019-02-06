package com.ringoid.domain.action_storage

import com.ringoid.domain.model.actions.ActionObject

interface IActionObjectPool {

    fun put(aobj: ActionObject)

    fun trigger()
}
