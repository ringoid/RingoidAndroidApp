package com.ringoid.origin.feed.view.lmm.messenger

import android.app.Application
import com.ringoid.analytics.Analytics
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.feed.GetCachedFeedItemByIdUseCase
import com.ringoid.domain.interactor.feed.TransferFeedItemUseCase
import com.ringoid.domain.interactor.feed.UpdateFeedItemAsSeenUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesForChatUseCase
import com.ringoid.domain.interactor.messenger.GetChatUseCase
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.domain.model.feed.LmmSlice
import com.ringoid.origin.feed.view.lc.PUSH_NEW_MESSAGES_TOTAL
import com.ringoid.origin.feed.view.lc.SEEN_ALL_FEED
import com.ringoid.origin.feed.view.lmm.base.BaseMatchesFeedViewModel
import com.ringoid.origin.view.main.LmmNavTab
import io.reactivex.Observable
import javax.inject.Inject

@Deprecated("LMM -> LC")
class MessagesFeedViewModel @Inject constructor(
    getChatUseCase: GetChatUseCase,
    getLmmUseCase: GetLmmUseCase,
    getCachedFeedItemByIdUseCase: GetCachedFeedItemByIdUseCase,
    updateFeedItemAsSeenUseCase: UpdateFeedItemAsSeenUseCase,
    transferFeedItemUseCase: TransferFeedItemUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    clearMessagesForChatUseCase: ClearMessagesForChatUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
    filtersSource: IFiltersSource, userInMemoryCache: IUserInMemoryCache, app: Application)
    : BaseMatchesFeedViewModel(
        getChatUseCase,
        getLmmUseCase,
        getCachedFeedItemByIdUseCase,
        updateFeedItemAsSeenUseCase,
        transferFeedItemUseCase,
        clearCachedAlreadySeenProfileIdsUseCase,
        clearMessagesForChatUseCase,
        cacheBlockedProfileIdUseCase,
        countUserImagesUseCase,
        filtersSource, userInMemoryCache, app) {

    override fun countNotSeen(feed: List<FeedItem>): List<String> =
        feed.takeIf { it.isNotEmpty() }
            ?.let { items ->
                items.map { it.id to it.countOfPeerMessages() }
                     .filter { it.second > 0 }
                     .filter { it.second > ChatInMemoryCache.getPeerMessagesCount(it.first) }
                     .map { it.first }
            } ?: emptyList()

    override fun getFeedFlag(): Int = SEEN_ALL_FEED.FEED_MESSENGER

    override fun getFeedFromLmm(lmm: Lmm): List<FeedItem> = lmm.messages

    override fun getSourceFeed(): LmmNavTab = LmmNavTab.MESSAGES

    override fun sourceBadge(): Observable<Boolean> =
        getLmmUseCase.repository.badgeMessenger
            .doAfterNext {
                if (it && getUserVisibleHint()) {
                    analyticsManager.fireOnce(Analytics.AHA_FIRST_MESSAGE_RECEIVED, "sourceFeed" to getFeedName())
                }
            }

    override fun sourceFeed(): Observable<LmmSlice> = getLmmUseCase.repository.feedMessages

    override fun getFeedName(): String = DomainUtil.SOURCE_FEED_MESSAGES

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun handleUserVisibleHint(isVisibleToUser: Boolean) {
        super.handleUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser /** switched to this Lmm tab */ && badgeIsOn /** has new feed items */) {
            analyticsManager.fireOnce(Analytics.AHA_FIRST_MESSAGE_RECEIVED, "sourceFeed" to getFeedName())
        }
    }

    // --------------------------------------------------------------------------------------------
    override fun handlePushMessage(peerId: String) {
        super.handlePushMessage(peerId)
        // show badge on Messages Lmm top tab
        viewState.value = ViewState.DONE(PUSH_NEW_MESSAGES_TOTAL)
        // show 'tap-to-refresh' popup on Feed screen
        refreshOnPush.value = feed().value?.isNotEmpty() == true
    }
}
