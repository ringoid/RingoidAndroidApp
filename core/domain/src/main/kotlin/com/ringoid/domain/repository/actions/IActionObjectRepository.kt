package com.ringoid.domain.repository.actions

import io.reactivex.Single

interface IActionObjectRepository {

    fun countCachedActionObjects(): Single<Int>
}
