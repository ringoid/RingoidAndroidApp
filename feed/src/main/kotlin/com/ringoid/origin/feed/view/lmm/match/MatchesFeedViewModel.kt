package com.ringoid.origin.feed.view.lmm.match

import android.app.Application
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.DropLmmChangedStatusUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.origin.feed.view.lmm.SEEN_ALL_FEED
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedViewModel
import io.reactivex.Observable
import javax.inject.Inject

class MatchesFeedViewModel @Inject constructor(
    getLmmUseCase: GetLmmUseCase, clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase, dropLmmChangedStatusUseCase: DropLmmChangedStatusUseCase,
    userInMemoryCache: IUserInMemoryCache, app: Application)
    : BaseLmmFeedViewModel(getLmmUseCase, clearCachedAlreadySeenProfileIdsUseCase, cacheBlockedProfileIdUseCase,
                           countUserImagesUseCase, dropLmmChangedStatusUseCase, userInMemoryCache, app) {

    override fun getFeedFlag(): Int = SEEN_ALL_FEED.FEED_MATCHES

    override fun getFeedFromLmm(lmm: Lmm): List<FeedItem> = lmm.matches

    override fun sourceFeed(): Observable<List<FeedItem>> = getLmmUseCase.repository.feedMatches

    override fun getFeedName(): String = DomainUtil.SOURCE_FEED_MATCHES

    // --------------------------------------------------------------------------------------------
    override fun onViewFeedItem(feedItemId: String) {
        super.onViewFeedItem(feedItemId)
        markFeedItemAsSeen(feedItemId = feedItemId)
    }
}
