package com.ringoid.origin.feed.view.explore

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.IListScrollCallback
import com.ringoid.base.view.ViewState
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.exception.ThresholdExceededException
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.*
import com.ringoid.domain.interactor.feed.property.GetCachedLmmFeedItemIdsUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesForChatUseCase
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.feed.Feed
import com.ringoid.origin.feed.view.DISCARD_PROFILE
import com.ringoid.origin.feed.view.DISCARD_PROFILES
import com.ringoid.origin.feed.view.FeedViewModel
import com.ringoid.origin.utils.ScreenHelper
import com.ringoid.origin.view.common.visual.LikeVisualEffect
import com.ringoid.origin.view.common.visual.VisualEffectManager
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject

class ExploreViewModel @Inject constructor(
    private val getNewFacesUseCase: GetNewFacesUseCase,
    private val getCachedLmmFeedItemIdsUseCase: GetCachedLmmFeedItemIdsUseCase,
    @DebugOnly private val debugGetNewFacesUseCase: DebugGetNewFacesUseCase,
    @DebugOnly private val debugGetNewFacesDropFlagsUseCase: DebugGetNewFacesDropFlagsUseCase,
    @DebugOnly private val debugGetNewFacesRepeatAfterDelayForPageUseCase: DebugGetNewFacesRepeatAfterDelayForPageUseCase,
    @DebugOnly private val debugGetNewFacesRetryNTimesForPageUseCase: DebugGetNewFacesRetryNTimesForPageUseCase,
    @DebugOnly private val debugGetNewFacesThresholdExceed: DebugGetNewFacesThresholdExceed,
    private val cacheAlreadySeenProfileIdsUseCase: CacheAlreadySeenProfileIdsUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    clearMessagesForChatUseCase: ClearMessagesForChatUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase, userInMemoryCache: IUserInMemoryCache, app: Application)
    : FeedViewModel(
        clearCachedAlreadySeenProfileIdsUseCase,
        clearMessagesForChatUseCase,
        cacheBlockedProfileIdUseCase,
        countUserImagesUseCase,
        userInMemoryCache, app), IListScrollCallback {

    val feed by lazy { MutableLiveData<Feed>() }
    private var isLoadingMore: Boolean = false
    private var nextPage: Int = 0

    override fun getFeedName(): String = DomainUtil.SOURCE_FEED_EXPLORE

    init {
        getNewFacesUseCase.repository.lmmLoadFinish
            .flatMap { getCachedLmmFeedItemIdsUseCase.source().toObservable() }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ ids ->
                viewState.value = ViewState.DONE(DISCARD_PROFILES(ids))
                if (BuildConfig.IS_STAGING) {
                    feed.value?.profiles?.toMutableList()
                        ?.let { profiles ->
                            val size = profiles.size
                            if (profiles.removeAll { it.id in ids }) {
                                DebugLogUtil.d("Removed ${size - profiles.size} profiles from NewFaces that already present in LMM [${ids.size}]")
                            }
                        }
                }
            }, Timber::e)
    }

    // --------------------------------------------------------------------------------------------
    override fun getFeed() {
//        debugGetNewFacesUseCase.source(params = prepareDebugFeedParams())
//        debugGetNewFacesRepeatAfterDelayForPageUseCase.source(params = prepareDebugFeedParamsRepeatAfterDelay())
//        debugGetNewFacesRetryNTimesForPageUseCase.source(params = prepareDebugFeedParamsRetryNTimes())
//        debugGetNewFacesThresholdExceed.source(params = prepareDebugFeedParamsThresholdExceed(failPage = 0))
        getNewFacesUseCase.source(params = prepareFeedParams())
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess {
                viewState.value = if (it.isEmpty()) ViewState.CLEAR(mode = ViewState.CLEAR.MODE_EMPTY_DATA)
                                  else ViewState.IDLE
            }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({ feed.value = it }, Timber::e)
    }

    private fun getMoreFeed() {
//        debugGetNewFacesUseCase.source(params = prepareDebugFeedParams())
//        debugGetNewFacesRepeatAfterDelayForPageUseCase.source(params = prepareDebugFeedParamsRepeatAfterDelay())
//        debugGetNewFacesRetryNTimesForPageUseCase.source(params = prepareDebugFeedParamsRetryNTimes())
//        debugGetNewFacesThresholdExceed.source(params = prepareDebugFeedParamsThresholdExceed(failPage = 2))
        getNewFacesUseCase.source(params = prepareFeedParams())
            .doOnSubscribe { viewState.value = ViewState.PAGING }
            .doOnSuccess { viewState.value = ViewState.IDLE }
            .doOnError {
                if (it is ThresholdExceededException) {
                    feed.value = Feed(profiles = emptyList())
                } else {
                    viewState.value = ViewState.ERROR(it)
                }
            }
            .doFinally { isLoadingMore = false }
            .autoDisposable(this)
            .subscribe({ feed.value = it }, Timber::e)
    }

    // ------------------------------------------
    private fun prepareFeedParams(): Params =
        Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
                .put("limit", DomainUtil.LIMIT_PER_PAGE)

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
    override fun onLike(profileId: String, imageId: String, isLiked: Boolean) {
        super.onLike(profileId, imageId, isLiked)
        // discard profile from feed after like / unlike (unlike is not possible, left for symmetry)
        viewState.value = ViewState.DONE(DISCARD_PROFILE(profileId = profileId))
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

    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventReStartWithTime(event: BusEvent.ReStartWithTime) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        if (event.msElapsed in 300000L..1557989300340L) {
            DebugLogUtil.i("App last open was more than 5 minutes ago, refresh Lmm...")
            onRefresh()  // app reopen leads Explore screen to refresh as well
        }
    }
}
