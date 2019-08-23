package com.ringoid.origin.feed.view.lmm.like

import android.app.Application
import com.ringoid.analytics.Analytics
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.feed.property.GetCachedFeedItemByIdUseCase
import com.ringoid.domain.interactor.feed.property.TransferFeedItemUseCase
import com.ringoid.domain.interactor.feed.property.UpdateFeedItemAsSeenUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesForChatUseCase
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.domain.model.feed.LmmSlice
import com.ringoid.origin.feed.view.lc.SEEN_ALL_FEED
import com.ringoid.origin.feed.view.lc.TRANSFER_PROFILE
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedViewModel
import com.ringoid.origin.view.common.visual.MatchVisualEffect
import com.ringoid.origin.view.common.visual.VisualEffectManager
import com.ringoid.origin.view.main.LmmNavTab
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Deprecated("LMM -> LC")
class LikesFeedViewModel @Inject constructor(
    getLmmUseCase: GetLmmUseCase,
    getCachedFeedItemByIdUseCase: GetCachedFeedItemByIdUseCase,
    updateFeedItemAsSeenUseCase: UpdateFeedItemAsSeenUseCase,
    transferFeedItemUseCase: TransferFeedItemUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    clearMessagesForChatUseCase: ClearMessagesForChatUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
    filtersSource: IFiltersSource, userInMemoryCache: IUserInMemoryCache, app: Application)
    : BaseLmmFeedViewModel(
        getLmmUseCase,
        getCachedFeedItemByIdUseCase,
        updateFeedItemAsSeenUseCase,
        transferFeedItemUseCase,
        clearCachedAlreadySeenProfileIdsUseCase,
        clearMessagesForChatUseCase,
        cacheBlockedProfileIdUseCase,
        countUserImagesUseCase,
        filtersSource, userInMemoryCache, app) {

    private val incomingPushLike = PublishSubject.create<BusEvent>()

    init {
        // show 'tap-to-refresh' popup on Feed screen
        incomingPushLike
            .debounce(DomainUtil.DEBOUNCE_PUSH, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ refreshOnPush.value = feed().value?.isNotEmpty() == true }, Timber::e)
    }

    override fun getFeedFlag(): Int = SEEN_ALL_FEED.FEED_LIKES

    override fun getFeedFromLmm(lmm: Lmm): List<FeedItem> = lmm.likes

    override fun getSourceFeed(): LmmNavTab = LmmNavTab.LIKES

    override fun sourceBadge(): Observable<Boolean> =
        getLmmUseCase.repository.badgeLikes
            .doAfterNext {
                if (it && getUserVisibleHint()) {
                    analyticsManager.fireOnce(Analytics.AHA_FIRST_LIKES_YOU, "sourceFeed" to getFeedName())
                }
            }

    override fun sourceFeed(): Observable<LmmSlice> = getLmmUseCase.repository.feedLikes

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
    override fun onLike(profileId: String, imageId: String) {
        super.onLike(profileId, imageId)
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

    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventPushNewLike(event: BusEvent.PushNewLike) {
        incomingPushLike.onNext(event)
    }
}
