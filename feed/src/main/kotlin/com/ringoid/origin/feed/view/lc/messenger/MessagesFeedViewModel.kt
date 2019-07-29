package com.ringoid.origin.feed.view.lc.messenger

import android.app.Application
import com.ringoid.base.manager.analytics.Analytics
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLcUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesForChatUseCase
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.origin.feed.view.lc.base.BaseLcFeedViewModel
import com.ringoid.origin.feed.view.lmm.SEEN_ALL_FEED
import com.ringoid.origin.view.main.LmmNavTab
import io.reactivex.Observable
import javax.inject.Inject

class MessagesFeedViewModel @Inject constructor(
    getLcUseCase: GetLcUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    clearMessagesForChatUseCase: ClearMessagesForChatUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
    userInMemoryCache: IUserInMemoryCache, app: Application)
    : BaseLcFeedViewModel(
        getLcUseCase,
        clearCachedAlreadySeenProfileIdsUseCase,
        clearMessagesForChatUseCase,
        cacheBlockedProfileIdUseCase,
        countUserImagesUseCase,
        userInMemoryCache, app) {

    override fun getFeedFlag(): Int = SEEN_ALL_FEED.FEED_MESSENGER

    override fun getFeedFromLmm(lmm: Lmm): List<FeedItem> = lmm.messages

    override fun getSourceFeed(): LmmNavTab = LmmNavTab.MESSAGES

    override fun getFeedName(): String = DomainUtil.SOURCE_FEED_MESSAGES

    override fun sourceBadge(): Observable<Boolean> =
        getLcUseCase.repository.badgeMessenger
            .doAfterNext {
                if (it && getUserVisibleHint()) {
                    analyticsManager.fireOnce(Analytics.AHA_FIRST_MESSAGE_RECEIVED, "sourceFeed" to getFeedName())
                }
            }

    override fun sourceFeed(): Observable<List<FeedItem>> = getLcUseCase.repository.feedMessages

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun handleUserVisibleHint(isVisibleToUser: Boolean) {
        super.handleUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser /** switched to this Lmm tab */ && badgeIsOn /** has new feed items */) {
            analyticsManager.fireOnce(Analytics.AHA_FIRST_MESSAGE_RECEIVED, "sourceFeed" to getFeedName())
        }
    }
}
