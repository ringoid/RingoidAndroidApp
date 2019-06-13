package com.ringoid.origin.feed.view.lmm.messenger

import android.app.Application
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.manager.analytics.Analytics
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.feed.property.*
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesForChatUseCase
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.origin.feed.view.lmm.SEEN_ALL_FEED
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedViewModel
import io.reactivex.Observable
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject

class MessagesFeedViewModel @Inject constructor(
    getLmmUseCase: GetLmmUseCase,
    getCachedFeedItemByIdUseCase: GetCachedFeedItemByIdUseCase,
    getLikedFeedItemIdsUseCase: GetLikedFeedItemIdsUseCase,
    getUserMessagedFeedItemIdsUseCase: GetUserMessagedFeedItemIdsUseCase,
    addLikedImageForFeedItemIdUseCase: AddLikedImageForFeedItemIdUseCase,
    addUserMessagedFeedItemIdUseCase: AddUserMessagedFeedItemIdUseCase,
    updateFeedItemAsSeenUseCase: UpdateFeedItemAsSeenUseCase,
    transferFeedItemUseCase: TransferFeedItemUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    clearMessagesForChatUseCase: ClearMessagesForChatUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
    notifyLmmProfileBlockedUseCase: NotifyProfileBlockedUseCase,
    userInMemoryCache: IUserInMemoryCache, app: Application)
    : BaseLmmFeedViewModel(
        getLmmUseCase,
        getCachedFeedItemByIdUseCase,
        getLikedFeedItemIdsUseCase,
        getUserMessagedFeedItemIdsUseCase,
        addLikedImageForFeedItemIdUseCase,
        addUserMessagedFeedItemIdUseCase,
        notifyLmmProfileBlockedUseCase,
        updateFeedItemAsSeenUseCase,
        transferFeedItemUseCase,
        clearCachedAlreadySeenProfileIdsUseCase,
        clearMessagesForChatUseCase,
        cacheBlockedProfileIdUseCase,
        countUserImagesUseCase,
        userInMemoryCache, app) {

    override fun countNotSeen(feed: List<FeedItem>): List<String> =
        feed.takeIf { it.isNotEmpty() }
            ?.let {
                it.map { it.id to it.countOfPeerMessages() }
                    .filter { it.second > 0 }
                    .filter { it.second != ChatInMemoryCache.getPeerMessagesCount(it.first) }
                    .map { it.first }
            } ?: emptyList()

    override fun getFeedFlag(): Int = SEEN_ALL_FEED.FEED_MESSENGER

    override fun getFeedFromLmm(lmm: Lmm): List<FeedItem> = lmm.messages

    override fun sourceBadge(): Observable<Boolean> =
        getLmmUseCase.repository.badgeMessenger
            .doAfterNext {
                if (it && getUserVisibleHint()) {
                    analyticsManager.fireOnce(Analytics.AHA_FIRST_MESSAGE_RECEIVED, "sourceFeed" to getFeedName())
                }
            }

    override fun sourceFeed(): Observable<List<FeedItem>> = getLmmUseCase.repository.feedMessages

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
    override fun onChatClose(profileId: String, imageId: String) {
        super.onChatClose(profileId, imageId)
        markFeedItemAsSeen(feedItemId = profileId)
    }

    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventPushNewMessage(event: BusEvent.PushNewMessage) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        if (ChatInMemoryCache.isChatOpen(chatId = event.peerId)) {
            Timber.v("Chat is currently open, skip push notification handling: ${event.peerId}")
            return  // consume push event and skip any updates if target Chat is currently open
        }

        refreshOnPush.value = feed.value?.isNotEmpty() == true  // show 'tap-to-refresh' popup on Feed screen
        /**
         * New messages have been received from push notification for profile with id [BusEvent.PushNewMessage.peerId],
         * so need to update corresponding feed item, if any, to visually reflect change in unread messages count.
         */
        ChatInMemoryCache.setPeerMessagesCount(profileId = event.peerId, count = 0)
        viewState.value = ViewState.DONE(PUSH_NEW_MESSAGES(profileId = event.peerId))
    }
}
