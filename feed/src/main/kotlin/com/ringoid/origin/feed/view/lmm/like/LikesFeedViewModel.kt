package com.ringoid.origin.feed.view.lmm.like

import android.app.Application
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.feed.property.*
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesForChatUseCase
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.origin.feed.view.lmm.SEEN_ALL_FEED
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedViewModel
import io.reactivex.Observable
import javax.inject.Inject

class LikesFeedViewModel @Inject constructor(
    getLmmUseCase: GetLmmUseCase,
    getLikedFeedItemIdsUseCase: GetLikedFeedItemIdsUseCase,
    getUserMessagedFeedItemIdsUseCase: GetUserMessagedFeedItemIdsUseCase,
    addLikedImageForFeedItemIdUseCase: AddLikedImageForFeedItemIdUseCase,
    addUserMessagedFeedItemIdUseCase: AddUserMessagedFeedItemIdUseCase,
    updateFeedItemAsSeenUseCase: UpdateFeedItemAsSeenUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    clearMessagesForChatUseCase: ClearMessagesForChatUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
    userInMemoryCache: IUserInMemoryCache, app: Application)
    : BaseLmmFeedViewModel(
        getLmmUseCase,
        getLikedFeedItemIdsUseCase,
        getUserMessagedFeedItemIdsUseCase,
        addLikedImageForFeedItemIdUseCase,
        addUserMessagedFeedItemIdUseCase,
        updateFeedItemAsSeenUseCase,
        clearCachedAlreadySeenProfileIdsUseCase,
        clearMessagesForChatUseCase,
        cacheBlockedProfileIdUseCase,
        countUserImagesUseCase,
        userInMemoryCache, app) {

    private val numberOfLikes = mutableMapOf<String, MutableSet<String>>()

    override fun getFeedFlag(): Int = SEEN_ALL_FEED.FEED_LIKES

    override fun getFeedFromLmm(lmm: Lmm): List<FeedItem> = lmm.likes

    override fun sourceFeed(): Observable<List<FeedItem>> = getLmmUseCase.repository.feedLikes

    override fun getFeedName(): String = DomainUtil.SOURCE_FEED_LIKES

    // --------------------------------------------------------------------------------------------
    fun onLike(profileId: String, imageId: String, isLiked: Boolean, feedItemPosition: Int) {
        if (!numberOfLikes.containsKey(profileId)) {
            numberOfLikes[profileId] = mutableSetOf()
        }
        numberOfLikes[profileId]?.let {
            if (isLiked) it.add(imageId) else it.remove(imageId)
            viewState.value = if (it.isEmpty()) ViewState.DONE(NO_LIKES_ON_PROFILE(feedItemPosition))
                              else ViewState.DONE(HAS_LIKES_ON_PROFILE(feedItemPosition))
        }
        onLike(profileId, imageId, isLiked)
    }

    override fun onViewFeedItem(feedItemId: String) {
        super.onViewFeedItem(feedItemId)
        markFeedItemAsSeen(feedItemId = feedItemId)
    }

    override fun onSettingsClick(profileId: String) {
        super.onSettingsClick(profileId)
        markFeedItemAsSeen(profileId)
    }
}
