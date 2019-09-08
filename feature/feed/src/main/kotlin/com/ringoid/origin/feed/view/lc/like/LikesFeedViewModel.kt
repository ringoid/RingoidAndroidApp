package com.ringoid.origin.feed.view.lc.like

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.analytics.Analytics
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.viewmodel.OneShot
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.interactor.feed.*
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesForChatUseCase
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.LmmSlice
import com.ringoid.origin.feed.misc.HandledPushDataInMemory
import com.ringoid.origin.feed.view.lc.base.BaseLcFeedViewModel
import com.ringoid.origin.view.common.visual.MatchVisualEffect
import com.ringoid.origin.view.common.visual.VisualEffectManager
import com.ringoid.origin.view.main.LcNavTab
import com.ringoid.report.log.Report
import com.ringoid.utility.runOnUiThread
import com.ringoid.utility.vibrate
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LikesFeedViewModel @Inject constructor(
    getLcUseCase: GetLcUseCase,
    getCachedFeedItemByIdUseCase: GetCachedFeedItemByIdUseCase,
    updateFeedItemAsSeenUseCase: UpdateFeedItemAsSeenUseCase,
    transferFeedItemUseCase: TransferFeedItemUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    clearMessagesForChatUseCase: ClearMessagesForChatUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
    filtersSource: IFiltersSource, userInMemoryCache: IUserInMemoryCache, app: Application)
    : BaseLcFeedViewModel(
        getLcUseCase,
        getCachedFeedItemByIdUseCase,
        updateFeedItemAsSeenUseCase,
        transferFeedItemUseCase,
        clearCachedAlreadySeenProfileIdsUseCase,
        clearMessagesForChatUseCase,
        cacheBlockedProfileIdUseCase,
        countUserImagesUseCase,
        filtersSource, userInMemoryCache, app) {

    private val incomingPushLike = PublishSubject.create<BusEvent>()
    private val incomingPushLikeEffect = PublishSubject.create<Long>()
    private val pushNewLike by lazy { MutableLiveData<Long>() }
    private val pushLikesBadgeOneShot by lazy { MutableLiveData<OneShot<Boolean>>() }
    private val transferProfileOneShot by lazy { MutableLiveData<OneShot<String>>() }
    internal fun pushNewLike(): LiveData<Long> = pushNewLike
    internal fun pushLikesBadgeOneShot(): LiveData<OneShot<Boolean>> = pushLikesBadgeOneShot
    internal fun transferProfileOneShot(): LiveData<OneShot<String>> = transferProfileOneShot

    private var shouldVibrate: Boolean = true

    init {
        // show 'tap-to-refresh' popup on Feed screen
        incomingPushLike
            .debounce(DomainUtil.DEBOUNCE_PUSH, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({
                // show badge on Likes LC tab
                pushLikesBadgeOneShot.value = OneShot(true)
                // show 'tap-to-refresh' popup on Feed screen
                refreshOnPush.value = true
            }, DebugLogUtil::e)

        // show particle animation and vibrate
        incomingPushLikeEffect
            .doOnNext { pushNewLike.value = 0L }  // for particle animation
            .throttleFirst(DomainUtil.DEBOUNCE_PUSH, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ if (!isStopped && shouldVibrate) app.vibrate() }, DebugLogUtil::e)
    }

    // ------------------------------------------
    override fun countNotSeen(feed: List<FeedItem>): List<String> =
        feed.filter { it.isNotSeen }.map { it.id }

    // ------------------------------------------
    override fun getSourceFeed(): LcNavTab = LcNavTab.LIKES
    override fun getFeedName(): String = DomainUtil.SOURCE_FEED_LIKES

    override fun sourceBadge(): Observable<Boolean> =
        getLcUseCase.repository.badgeLikesSource()
            .doAfterNext {
                if (it && getUserVisibleHint()) {
                    analyticsManager.fireOnce(Analytics.AHA_FIRST_LIKES_YOU, "sourceFeed" to getFeedName())
                }
            }

    override fun sourceFeed(): Observable<LmmSlice> = getLcUseCase.repository.feedLikesSource()
        .doAfterNext {
            runOnUiThread {
                if (it.totalNotFilteredCount >= 15 && getUserVisibleHint() &&
                    spm.needShowFiltersOnLc() /** check flag once and drop */ ) {
                    needShowFiltersOneShot.value = OneShot(true)
                }
            }
        }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun handleUserVisibleHint(isVisibleToUser: Boolean) {
        super.handleUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {  /** switched to this LC tab */
            if (badgeIsOn) {  /** has new feed items */
                analyticsManager.fireOnce(Analytics.AHA_FIRST_LIKES_YOU, "sourceFeed" to getFeedName())
            }
            shouldVibrate = spm.getUserPushSettings().pushVibration
        }
    }

    override fun onStart() {
        super.onStart()
        shouldVibrate = spm.getUserPushSettings().pushVibration
    }

    // --------------------------------------------------------------------------------------------
    override fun onLike(profileId: String, imageId: String) {
        super.onLike(profileId, imageId)
        // transfer liked profile from Likes Feed to Matches Feed, by Product
        transferProfileOneShot.value = OneShot(profileId)
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

    /* Event Bus */
    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventPushNewLike(event: BusEvent.PushNewLike) {
        Timber.d("Received bus event: $event")
        Report.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        HandledPushDataInMemory.incrementCountOfHandledPushLikes()
        incomingPushLike.onNext(event)  // for 'tap-to-refresh' popup
        incomingPushLikeEffect.onNext(0L)
    }
}
