package com.ringoid.origin.feed.view.lmm.match

import android.app.Application
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.manager.analytics.Analytics
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.feed.property.GetCachedFeedItemByIdUseCase
import com.ringoid.domain.interactor.feed.property.NotifyProfileBlockedUseCase
import com.ringoid.domain.interactor.feed.property.TransferFeedItemUseCase
import com.ringoid.domain.interactor.feed.property.UpdateFeedItemAsSeenUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesForChatUseCase
import com.ringoid.domain.interactor.messenger.GetChatUseCase
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.origin.feed.view.lmm.SEEN_ALL_FEED
import com.ringoid.origin.feed.view.lmm.base.BaseMatchesFeedViewModel
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

class MatchesFeedViewModel @Inject constructor(
    getChatUseCase: GetChatUseCase,
    getLmmUseCase: GetLmmUseCase,
    getCachedFeedItemByIdUseCase: GetCachedFeedItemByIdUseCase,
    updateFeedItemAsSeenUseCase: UpdateFeedItemAsSeenUseCase,
    transferFeedItemUseCase: TransferFeedItemUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    clearMessagesForChatUseCase: ClearMessagesForChatUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
    notifyLmmProfileBlockedUseCase: NotifyProfileBlockedUseCase,
    userInMemoryCache: IUserInMemoryCache, app: Application)
    : BaseMatchesFeedViewModel(
        getChatUseCase,
        getLmmUseCase,
        getCachedFeedItemByIdUseCase,
        notifyLmmProfileBlockedUseCase,
        updateFeedItemAsSeenUseCase,
        transferFeedItemUseCase,
        clearCachedAlreadySeenProfileIdsUseCase,
        clearMessagesForChatUseCase,
        cacheBlockedProfileIdUseCase,
        countUserImagesUseCase,
        userInMemoryCache, app) {

    private val incomingPushMatch = PublishSubject.create<BusEvent>()

    init {
        // show 'tap-to-refresh' popup on Feed screen
        incomingPushMatch
            .debounce(DomainUtil.DEBOUNCE_PUSH, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ refreshOnPush.value = feed.value?.isNotEmpty() == true }, Timber::e)
    }

    override fun getFeedFlag(): Int = SEEN_ALL_FEED.FEED_MATCHES

    override fun getFeedFromLmm(lmm: Lmm): List<FeedItem> = lmm.matches

    override fun getSourceFeed(): LmmNavTab = LmmNavTab.MATCHES

    override fun sourceBadge(): Observable<Boolean> =
        getLmmUseCase.repository.badgeMatches
            .doAfterNext {
                if (it && getUserVisibleHint()) {
                    analyticsManager.fireOnce(Analytics.AHA_FIRST_MATCH, "sourceFeed" to getFeedName())
                }
            }

    override fun sourceFeed(): Observable<List<FeedItem>> = getLmmUseCase.repository.feedMatches

    override fun getFeedName(): String = DomainUtil.SOURCE_FEED_MATCHES

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun handleUserVisibleHint(isVisibleToUser: Boolean) {
        super.handleUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser /** switched to this Lmm tab */ && badgeIsOn /** has new feed items */) {
            analyticsManager.fireOnce(Analytics.AHA_FIRST_MATCH, "sourceFeed" to getFeedName())
        }
    }

    // --------------------------------------------------------------------------------------------
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
    fun onEventPushNewMatch(event: BusEvent.PushNewMatch) {
        incomingPushMatch.onNext(event)
    }
}
