package com.ringoid.domain.repository.debug

import com.ringoid.domain.model.feed.Feed
import io.reactivex.Completable
import io.reactivex.Single

interface IDebugRepository {

    fun requestWithFailNTimesBeforeSuccess(count: Int): Completable
    fun requestWithRepeatAfterDelay(delay: Long): Completable
    fun requestWithInvalidAccessToken(token: String): Completable
    fun requestWithUnsupportedAppVersion(): Completable
    fun requestWithWrongParameters(): Completable

    fun debugRequestTimeOut(): Completable
    fun debugNotSuccessResponse(): Completable
    fun debugRequestCausingServerError(): Completable
    fun debugRequestWithInvalidAccessToken(): Completable
    fun debugRequestWithUnsupportedAppVersion(): Completable

    // ------------------------------------------
    fun debugGetNewFaces(page: Int): Single<Feed>
}
