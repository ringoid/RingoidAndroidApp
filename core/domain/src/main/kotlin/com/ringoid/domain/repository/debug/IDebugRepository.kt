package com.ringoid.domain.repository.debug

import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.model.feed.Feed
import io.reactivex.Completable
import io.reactivex.Single

@DebugOnly
interface IDebugRepository {

    fun commitActionObjectsWithFailAllRetries(): Completable

    fun requestWithFailAllRetries(): Completable
    fun requestWithFailNTimesBeforeSuccess(count: Int): Completable
    fun requestWithRepeatAfterDelay(delay: Long): Completable
    fun requestWithInvalidAccessToken(token: String): Completable
    fun requestWithUnsupportedAppVersion(): Completable
    fun requestWithWrongParameters(): Completable

    fun debugRequestTimeOut(): Completable
    fun debugNotSuccessResponse(): Completable
    fun debugResponseWith404(): Completable
    fun debugRequestCausingServerError(): Completable
    fun debugRequestWithInvalidAccessToken(): Completable
    fun debugRequestWithUnsupportedAppVersion(): Completable

    // ------------------------------------------
    fun debugHandleErrorDownstream(): Completable
    fun debugHandleErrorMultistream(): Completable
    fun debugHandleErrorUpstream(): Completable

    // ------------------------------------------
    fun debugGetNewFaces(page: Int): Single<Feed>
    fun debugGetNewFacesFail(): Single<Feed>
    fun debugGetNewFacesFailAndRecoverAfterNTimes(count: Int): Single<Feed>

    fun dropFlags(): Completable
}
