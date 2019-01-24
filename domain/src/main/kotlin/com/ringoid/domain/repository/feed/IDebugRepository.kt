package com.ringoid.domain.repository.feed

import io.reactivex.Completable

interface IDebugRepository {

    fun requestWithInvalidAccessToken(token: String): Completable
    fun requestWithUnsupportedAppVersion(): Completable
    fun requestWithWrongParameters(): Completable
}
