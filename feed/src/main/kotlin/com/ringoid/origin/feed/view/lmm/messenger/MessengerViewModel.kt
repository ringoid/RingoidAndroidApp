package com.ringoid.origin.feed.view.lmm.messenger

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.manager.analytics.Analytics
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
import com.ringoid.origin.feed.misc.HandledPushDataInMemory
import com.ringoid.origin.feed.view.lmm.SEEN_ALL_FEED
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedViewModel
import io.reactivex.Observable
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject

class MessengerViewModel @Inject constructor(
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
    userInMemoryCache: IUserInMemoryCache, app: Application)
    : BaseLmmFeedViewModel(
        getLmmUseCase,
        getCachedFeedItemByIdUseCase,
        getLikedFeedItemIdsUseCase,
        getUserMessagedFeedItemIdsUseCase,
        addLikedImageForFeedItemIdUseCase,
        addUserMessagedFeedItemIdUseCase,
        updateFeedItemAsSeenUseCase,
        transferFeedItemUseCase,
        clearCachedAlreadySeenProfileIdsUseCase,
        clearMessagesForChatUseCase,
        cacheBlockedProfileIdUseCase,
        countUserImagesUseCase,
        userInMemoryCache, app) {

    internal val pushNewMessage by lazy { MutableLiveData<Long>() }

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
    fun onEventPushNewLike(event: BusEvent.PushNewMessage) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        HandledPushDataInMemory.incrementCountOfHandledPushMessages()
        pushNewMessage.value = 0L  // for particle animation
        refreshOnPush.value = true
    }
}
