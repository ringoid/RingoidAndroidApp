package com.ringoid.origin.feed.view.lmm.like

import android.app.Application
import com.ringoid.base.manager.analytics.Analytics
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
import com.ringoid.origin.feed.view.lmm.TRANSFER_PROFILE
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedViewModel
import com.ringoid.origin.view.common.visual.MatchVisualEffect
import com.ringoid.origin.view.common.visual.VisualEffectManager
import io.reactivex.Observable
import javax.inject.Inject

class LikesFeedViewModel @Inject constructor(
    getLmmUseCase: GetLmmUseCase,
    getCachedFeedItemByIdUseCase: GetCachedFeedItemByIdUseCase,
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
        getCachedFeedItemByIdUseCase,
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

    override fun getFeedFlag(): Int = SEEN_ALL_FEED.FEED_LIKES

    override fun getFeedFromLmm(lmm: Lmm): List<FeedItem> = lmm.likes

    override fun sourceBadge(): Observable<Boolean> =
        getLmmUseCase.repository.badgeLikes
            .doAfterNext {
                if (it && getUserVisibleHint()) {
                    analyticsManager.fireOnce(Analytics.AHA_FIRST_LIKES_YOU, "sourceFeed" to getFeedName())
                }
            }

    override fun sourceFeed(): Observable<List<FeedItem>> = getLmmUseCase.repository.feedLikes

    override fun getFeedName(): String = DomainUtil.SOURCE_FEED_LIKES

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun handleUserVisibleHint(isVisibleToUser: Boolean) {
        super.handleUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser /** switched to this Lmm tab */ && badgeIsOn /** has new feed items */) {
            analyticsManager.fireOnce(Analytics.AHA_FIRST_LIKES_YOU, "sourceFeed" to getFeedName())
        }
    }

    // --------------------------------------------------------------------------------------------
    fun onLike(profileId: String, imageId: String, isLiked: Boolean, feedItemPosition: Int) {
        onLike(profileId, imageId, isLiked)

        // transfer liked profile from Likes Feed to Matches Feed, by Product
        viewState.value = ViewState.DONE(TRANSFER_PROFILE(profileId = profileId))
    }

    override fun onImageTouch(x: Float, y: Float) {
        super.onImageTouch(x, y)
        VisualEffectManager.call(MatchVisualEffect(x, y))
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
