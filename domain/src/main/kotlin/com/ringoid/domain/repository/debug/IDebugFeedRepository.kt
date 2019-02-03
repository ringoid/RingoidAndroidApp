package com.ringoid.domain.repository.debug

import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Completable
import io.reactivex.Single

interface IDebugFeedRepository : IFeedRepository {

    fun debugGetNewFacesWithFailNTimesBeforeSuccessForPage(page: Int, failPage: Int, count: Int): Single<Feed>
    fun debugGetNewFacesWithRepeatForPageAfterDelay(page: Int, repeatPage: Int, delay: Long): Single<Feed>

    fun dropFlags(): Completable
}
