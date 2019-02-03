package com.ringoid.data.repository.debug

import com.ringoid.data.action_storage.ActionObjectPool
import com.ringoid.data.local.database.dao.feed.UserFeedDao
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.remote.model.feed.FeedResponse
import com.ringoid.data.remote.model.feed.ProfileEntity
import com.ringoid.data.remote.model.image.ImageEntity
import com.ringoid.data.repository.feed.FeedRepository
import com.ringoid.data.repository.handleError
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.domain.repository.debug.IDebugFeedRepository
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

class DebugFeedRepository @Inject constructor(
    @Named("alreadySeen") alreadySeenProfilesCache: UserFeedDao,
    @Named("block") blockedProfilesCache: UserFeedDao,
    cloud: RingoidCloud, spm: ISharedPrefsManager, aObjPool: ActionObjectPool)
    : FeedRepository(alreadySeenProfilesCache, blockedProfilesCache, cloud, spm, aObjPool), IDebugFeedRepository {

    /* Debug */
    // --------------------------------------------------------------------------------------------
    private var requestAttempt: Int = 0
    private var requestRepeatAfterDelayAttempt: Int = 0

    private fun getAndIncrementRequestAttempt(): Int = requestAttempt++
    private fun getAndIncrementRequestRepeatAfterDelayAttempt(): Int = requestRepeatAfterDelayAttempt++

    override fun debugGetNewFacesWithFailNTimesBeforeSuccessForPage(page: Int, failPage: Int, count: Int): Single<Feed> =
        if (page == failPage) {
            Single.just(DebugRepository.getFeed(page))
                .map {
                    val i = getAndIncrementRequestAttempt()
                    if (i < count) it.convertToFeedResponse(errorCode = "DebugError", errorMessage = "Debug error")
                    else it.convertToFeedResponse()
                }
        } else {
            Single.just(DebugRepository.getFeed(page)).map { it.convertToFeedResponse() }
        }
        .handleError(count = count * 2, delay = 250L)
        .filterAlreadySeenProfilesFeed()
        .filterBlockedProfilesFeed()
        .cacheNewFacesAsAlreadySeen()
        .map { it.map() }

    override fun debugGetNewFacesWithRepeatForPageAfterDelay(page: Int, repeatPage: Int, delay: Long): Single<Feed> =
        if (page == repeatPage) {
            val i = getAndIncrementRequestRepeatAfterDelayAttempt()
            Single.just(DebugRepository.getFeed(page))
                .map { it.convertToFeedResponse(repeatAfterSec = if (i < 1) delay else 0) }
        } else {
            Single.just(DebugRepository.getFeed(page)).map { it.convertToFeedResponse() }
        }
        .handleError()
        .filterAlreadySeenProfilesFeed()
        .filterBlockedProfilesFeed()
        .cacheNewFacesAsAlreadySeen()
        .map { it.map() }

    // ------------------------------------------
    private fun Feed.convertToFeedResponse(errorCode: String = "", errorMessage: String = "", repeatAfterSec: Long = 0L): FeedResponse =
        FeedResponse(profiles = this.profiles.map { ProfileEntity(id = it.id, sortPosition = 0, images = it.images.map { ImageEntity(id = it.id, uri = it.uri ?: "") }) }, errorCode = errorCode, errorMessage = errorMessage, repeatAfterSec = repeatAfterSec)
}
