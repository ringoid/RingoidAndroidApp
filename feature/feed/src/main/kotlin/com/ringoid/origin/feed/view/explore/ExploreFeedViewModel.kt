package com.ringoid.origin.feed.view.explore

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.analytics.Analytics
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.IListScrollCallback
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.OneShot
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.DomainUtil
import com.ringoid.debug.DebugLogUtil
import com.ringoid.report.exception.ErrorConnectionTimedOut
import com.ringoid.report.exception.ThresholdExceededException
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.*
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesForChatUseCase
import com.ringoid.report.log.SentryUtil
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.feed.Feed
import com.ringoid.origin.feed.exception.LoadMoreFailedException
import com.ringoid.origin.feed.view.FeedViewModel
import com.ringoid.origin.utils.ScreenHelper
import com.ringoid.origin.view.common.visual.LikeVisualEffect
import com.ringoid.origin.view.common.visual.VisualEffectManager
import com.ringoid.utility.DebugOnly
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject

class ExploreFeedViewModel @Inject constructor(
    private val getDiscoverUseCase: GetDiscoverUseCase,
    private val getCachedLmmFeedItemIdsUseCase: GetCachedLmmFeedItemIdsUseCase,
    @DebugOnly private val debugGetNewFacesUseCase: DebugGetNewFacesUseCase,
    @DebugOnly private val debugGetNewFacesFailedUseCase: DebugGetNewFacesFailedUseCase,
    @DebugOnly private val debugGetNewFacesFailedAndRecoverAfterNTimesUseCase: DebugGetNewFacesFailedAndRecoverAfterNTimesUseCase,
    @DebugOnly private val debugGetNewFacesDropFlagsUseCase: DebugGetNewFacesDropFlagsUseCase,
    @DebugOnly private val debugGetNewFacesRepeatAfterDelayForPageUseCase: DebugGetNewFacesRepeatAfterDelayForPageUseCase,
    @DebugOnly private val debugGetNewFacesRetryNTimesForPageUseCase: DebugGetNewFacesRetryNTimesForPageUseCase,
    @DebugOnly private val debugGetNewFacesThresholdExceed: DebugGetNewFacesThresholdExceed,
    private val cacheAlreadySeenProfileIdsUseCase: CacheAlreadySeenProfileIdsUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    clearMessagesForChatUseCase: ClearMessagesForChatUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
    filtersSource: IFiltersSource, userInMemoryCache: IUserInMemoryCache, app: Application)
    : FeedViewModel(
        clearCachedAlreadySeenProfileIdsUseCase,
        clearMessagesForChatUseCase,
        cacheBlockedProfileIdUseCase,
        countUserImagesUseCase,
        filtersSource, userInMemoryCache, app), IListScrollCallback {

    private val feed by lazy { MutableLiveData<Feed>() }
    private val discardProfilesOneShot by lazy { MutableLiveData<OneShot<Collection<String>>>() }
    internal fun feed(): LiveData<Feed> = feed
    internal fun discardProfilesOneShot(): LiveData<OneShot<Collection<String>>> = discardProfilesOneShot

    private var isLoadingMore: Boolean = false
    private var nextPage: Int = 0

    override fun getFeedName(): String = DomainUtil.SOURCE_FEED_EXPLORE

    init {
        // discard profiles that appear in Lmm from Explore feed
        getDiscoverUseCase.repository.lmmLoadFinish
            .flatMap { getCachedLmmFeedItemIdsUseCase.source().toObservable() }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ ids ->
                discardProfilesOneShot.value = OneShot(ids)
                if (BuildConfig.IS_STAGING) {
                    feed.value?.profiles?.toMutableList()
                        ?.let { profiles ->
                            val size = profiles.size
                            if (profiles.removeAll { it.id in ids }) {
                                DebugLogUtil.d("Removed ${size - profiles.size} profiles from NewFaces that already present in LMM [${ids.size}]")
                            }
                        }
                }
            }, DebugLogUtil::e)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onRecreate(savedInstanceState: Bundle) {
        super.onRecreate(savedInstanceState)
        refresh()  // TODO: do proper state restore
    }

    // --------------------------------------------------------------------------------------------
    override fun getFeed() {
//        debugGetNewFacesUseCase.source(params = prepareDebugFeedParams())
//        debugGetNewFacesFailedUseCase.source()
//        debugGetNewFacesRepeatAfterDelayForPageUseCase.source(params = prepareDebugFeedParamsRepeatAfterDelay())
//        debugGetNewFacesRetryNTimesForPageUseCase.source(params = prepareDebugFeedParamsRetryNTimes())
//        debugGetNewFacesThresholdExceed.source(params = prepareDebugFeedParamsThresholdExceed(failPage = 0))
        getDiscoverUseCase.source(params = prepareFeedParams())
            .doOnSubscribe { viewState.value = ViewState.LOADING }  // load feed items progress
            .doOnSuccess {
                viewState.value = if (it.isEmpty()) {
                    if (hasFiltersApplied()) {
                        ViewState.CLEAR(mode = ViewState.CLEAR.MODE_CHANGE_FILTERS)  // set empty Explore feed due to filters
                    } else {
                        analyticsManager.fire(Analytics.EMPTY_FEED_DISCOVER_NO_FILTERS)
                        ViewState.CLEAR(mode = ViewState.CLEAR.MODE_EMPTY_DATA)  // set empty Explore feed (no filters)
                    }
                } else ViewState.IDLE  // load feed items success

                if (spm.needShowFilters()) {
                    needShowFiltersOneShot.value = OneShot(true)
                }
            }
            .doOnError {
                viewState.value = ViewState.ERROR(it)  // load feed items failed
                when (it) {
                    is ErrorConnectionTimedOut -> analyticsManager.fire(Analytics.CONNECTION_TIMEOUT, "sourceFeed" to getFeedName())
                }
            }
            .autoDisposable(this)
            .subscribe({ feed.value = it }, DebugLogUtil::e)
    }

    private fun getMoreFeed() {
//        debugGetNewFacesUseCase.source(params = prepareDebugFeedParams())
//        debugGetNewFacesFailedUseCase.source()
//        debugGetNewFacesFailedAndRecoverAfterNTimesUseCase.source(params = Params().put("count", 2))
//        debugGetNewFacesRepeatAfterDelayForPageUseCase.source(params = prepareDebugFeedParamsRepeatAfterDelay())
//        debugGetNewFacesRetryNTimesForPageUseCase.source(params = prepareDebugFeedParamsRetryNTimes())
//        debugGetNewFacesThresholdExceed.source(params = prepareDebugFeedParamsThresholdExceed(failPage = 2))
        getDiscoverUseCase.source(params = prepareFeedParams())
            .doOnSubscribe { viewState.value = ViewState.PAGING }  // load more feed items
            .doOnSuccess { viewState.value = ViewState.IDLE }  // load more feed items success
            .doOnError {
                if (it is ThresholdExceededException) {
                    feed.value = Feed(profiles = emptyList())
                } else {
                    when (it) {
                        is ErrorConnectionTimedOut -> analyticsManager.fire(Analytics.CONNECTION_TIMEOUT, "sourceFeed" to getFeedName(), "state" to "load more items")
                    }
                    DebugLogUtil.e(it, "Failed to load more feed items")
                    viewState.value = ViewState.ERROR(LoadMoreFailedException(it))  // load more feed items failed
                }
            }
            .doFinally { isLoadingMore = false }
            .autoDisposable(this)
            .subscribe({ feed.value = it }, DebugLogUtil::e)
    }

    // ------------------------------------------
    internal fun onApplyFilters() {
        refresh()  // just refresh, filters are always applied on Explore feed
    }

    internal fun hasFiltersApplied(): Boolean = filtersSource.hasFiltersApplied()

    private fun prepareFeedParams(): Params =
        Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
                .put("limit", DomainUtil.LIMIT_PER_PAGE)
                .put(filtersSource.getFilters())

    @DebugOnly
    private fun prepareDebugFeedParams(): Params = Params().put("page", nextPage++)

    @DebugOnly
    private fun prepareDebugFeedParamsRepeatAfterDelay(): Params =
        Params().put("page", nextPage++)
                .put("repeatPage", 2)
                .put("delay", 5L)  // in seconds

    @DebugOnly
    private fun prepareDebugFeedParamsRetryNTimes(): Params =
        Params().put("page", nextPage++)
                .put("failPage", 2)
                .put("count", 2)

    @DebugOnly
    private fun prepareDebugFeedParamsThresholdExceed(failPage: Int): Params =
        Params().put("page", nextPage++)
                .put("failPage", failPage)

    // --------------------------------------------------------------------------------------------
    override fun onLike(profileId: String, imageId: String) {
        super.onLike(profileId, imageId)
        // discard profile from feed after like
        discardProfileOneShot.value = OneShot(profileId)
    }

    // ------------------------------------------
    override fun onRefresh() {
        super.onRefresh()
        nextPage = 0
        debugGetNewFacesDropFlagsUseCase.source()
            .autoDisposable(this)
            .subscribe()
    }

    override fun checkImagesCount(count: Int): Boolean = true  // always allow to get feed regardless of count of images

    // ------------------------------------------
    override fun onScroll(itemsLeftToEnd: Int) {
        if (isLoadingMore) {
            Timber.v("Skip onScroll event in error state or during loading more")
            return
        }
        if (itemsLeftToEnd <= DomainUtil.LOAD_MORE_THRESHOLD) {
            DebugLogUtil.b("Load next page")
            isLoadingMore = true
//            actionObjectPool.trigger()
            getMoreFeed()
        }
    }

    override fun onImageTouch(x: Float, y: Float) {
        super.onImageTouch(x, y)
        VisualEffectManager.call(LikeVisualEffect(x, y))
    }

    internal fun onRetryLoadMore() {
        DebugLogUtil.v("User pressed to retry load more feed items...")
        getMoreFeed()
    }

    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventAppFreshStart(event: BusEvent.AppFreshStart) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        DebugLogUtil.i("Get Discover on Application fresh start [${getFeedName()}]")
        refresh()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventRecreateMainScreen(event: BusEvent.RecreateMainScreen) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        DebugLogUtil.i("Get Discover on Application recreate while running [${getFeedName()}]")
        refresh()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventReOpenApp(event: BusEvent.ReOpenAppOnPush) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        DebugLogUtil.i("Get Discover on Application reopen [${getFeedName()}]")
        refresh()  // app reopen leads Explore screen to refresh as well
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventReStartWithTime(event: BusEvent.ReStartWithTime) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        if (event.msElapsed in 300000L..1557989300340L) {
            DebugLogUtil.i("App last open was more than 5 minutes ago, refresh Explore...")
            refresh()  // app reopen leads Explore screen to refresh as well
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventFiltersChangesInSettings(event: BusEvent.FiltersChangesInSettings) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        onApplyFilters()
    }
}
