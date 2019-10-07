package com.ringoid.domain.repository.actions

import io.reactivex.Completable
import io.reactivex.Single

interface IActionObjectRepository {

    fun countCachedActionObjects(): Single<Int>

    fun deleteActionObjectsForType(type: String): Completable

    fun deleteAllActionObjects(): Completable
}
