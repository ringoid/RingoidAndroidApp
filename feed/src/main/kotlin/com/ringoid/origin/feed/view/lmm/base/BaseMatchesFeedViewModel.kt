package com.ringoid.origin.feed.view.lmm.base

import android.app.Application
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.ViewState
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.feed.property.*
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesForChatUseCase
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.origin.feed.view.lmm.messenger.PUSH_NEW_MESSAGES
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

abstract class BaseMatchesFeedViewModel(
    getLmmUseCase: GetLmmUseCase,
    getCachedFeedItemByIdUseCase: GetCachedFeedItemByIdUseCase,
    getLikedFeedItemIdsUseCase: GetLikedFeedItemIdsUseCase,
    getUserMessagedFeedItemIdsUseCase: GetUserMessagedFeedItemIdsUseCase,
    addLikedImageForFeedItemIdUseCase: AddLikedImageForFeedItemIdUseCase,
    addUserMessagedFeedItemIdUseCase: AddUserMessagedFeedItemIdUseCase,
    notifyLmmProfileBlockedUseCase: NotifyProfileBlockedUseCase,
    updateFeedItemAsSeenUseCase: UpdateFeedItemAsSeenUseCase,
    transferFeedItemUseCase: TransferFeedItemUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    clearMessagesForChatUseCase: ClearMessagesForChatUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
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

    // --------------------------------------------------------------------------------------------
    override fun onChatClose(profileId: String, imageId: String) {
        super.onChatClose(profileId, imageId)
        markFeedItemAsSeen(feedItemId = profileId)
    }

    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    open fun onEventPushNewMessage(event: BusEvent.PushNewMessage) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        if (ChatInMemoryCache.isChatOpen(chatId = event.peerId)) {
            Timber.v("Chat is currently open, skip push notification handling: ${event.peerId}")
            return  // consume push event and skip any updates if target Chat is currently open
        }

        /**
         * New messages have been received from push notification for profile with id [BusEvent.PushNewMessage.peerId],
         * so need to update corresponding feed item, if any, to visually reflect change in unread messages count.
         */
        ChatInMemoryCache.setPeerMessagesCount(profileId = event.peerId, count = 0)
        viewState.value = ViewState.DONE(PUSH_NEW_MESSAGES(profileId = event.peerId))
    }
}