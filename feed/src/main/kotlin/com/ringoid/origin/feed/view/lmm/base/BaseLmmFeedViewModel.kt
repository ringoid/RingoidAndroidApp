package com.ringoid.origin.feed.view.lmm.base

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.ViewState
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.exception.ThresholdExceededException
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.feed.property.*
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.origin.feed.view.FeedViewModel
import com.ringoid.origin.feed.view.lmm.RESTORE_CACHED_LIKES
import com.ringoid.origin.feed.view.lmm.RESTORE_CACHED_USER_MESSAGES
import com.ringoid.origin.feed.view.lmm.SEEN_ALL_FEED
import com.ringoid.origin.utils.ScreenHelper
import com.ringoid.utility.runOnUiThread
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

/**
 * Cached feed represented with [Lmm] is used when fetching of original feed has failed,
 * basically due to exceeded request threshold, that is actually expressed via [ThresholdExceededException].
 * But this is only done upon refreshing the feed. Before that user could like some feed items
 * and / or compose the very first message to some peers (which then changes appearance of chat icon).
 * Thus, upon failed to refresh, feed is restored from the cache, but that cache lacks this information,
 * so that information is kept in special sources such as [getLikedFeedItemIdsUseCase] and [messagedFeedItemIds]
 * and hence could be applied on a cached feed, restoring the information to user.
 */
abstract class BaseLmmFeedViewModel(
    protected val getLmmUseCase: GetLmmUseCase,
    private val getLikedFeedItemIdsUseCase: GetLikedFeedItemIdsUseCase,
    private val getUserMessagedFeedItemIdsUseCase: GetUserMessagedFeedItemIdsUseCase,
    private val addLikedImageForFeedItemIdUseCase: AddLikedImageForFeedItemIdUseCase,
    private val addUserMessagedFeedItemIdUseCase: AddUserMessagedFeedItemIdUseCase,
    private val updateFeedItemAsSeenUseCase: UpdateFeedItemAsSeenUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
    userInMemoryCache: IUserInMemoryCache, app: Application)
    : FeedViewModel(
        clearCachedAlreadySeenProfileIdsUseCase,
        cacheBlockedProfileIdUseCase, countUserImagesUseCase,
        userInMemoryCache, app) {

    val feed by lazy { MutableLiveData<List<FeedItem>>() }
    private var notSeenFeedItemIds = mutableSetOf<String>()

    init {
        sourceFeed()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { setLmmItems(items = it, clearMode = ViewState.CLEAR.MODE_EMPTY_DATA) }
            .flatMap {
                // get cached feed items that were liked, to restore likes on cached feed
                val params = Params().put("feedItemIds", it.map { it.id })
                getLikedFeedItemIdsUseCase.source(params = params).toObservable()
            }
            .doOnNext { it.ids.takeIf { it.isNotEmpty() }?.let { viewState.value = ViewState.DONE(RESTORE_CACHED_LIKES(it)) } }
            .flatMap {
                // cached feed items that have only user messages inside, sent after receiving feed
                getUserMessagedFeedItemIdsUseCase.source().toObservable()
            }
            .doOnNext { it.takeIf { it.isNotEmpty() }?.let { viewState.value = ViewState.DONE(RESTORE_CACHED_USER_MESSAGES(it)) } }
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    protected open fun countNotSeen(feed: List<FeedItem>): List<String> =
        feed.filter { it.isNotSeen }.map { it.id }

    protected abstract fun getFeedFlag(): Int
    protected abstract fun getFeedFromLmm(lmm: Lmm): List<FeedItem>
    protected abstract fun sourceFeed(): Observable<List<FeedItem>>

    override fun getFeed() {
        val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
                             .put("source", getFeedName())

        getLmmUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    fun applyCachedFeed(lmm: Lmm?) {
        lmm?.let { setLmmItems(getFeedFromLmm(it)) } ?: run { setLmmItems(emptyList()) }
    }

    private fun setLmmItems(items: List<FeedItem>, clearMode: Int = ViewState.CLEAR.MODE_NEED_REFRESH) {
        if (items.isEmpty()) {
            viewState.value = ViewState.CLEAR(mode = clearMode)
        } else {
            if (BuildConfig.DEBUG) {
                Timber.v(items.joinToString("\n\t\t", "\t*** LMM ***\n\t\t", "\n\t***\n", transform = { "LMM: ${it.toShortString()} :: ${getFeedName()}" }))
            }
            feed.value = items
            viewState.value = ViewState.IDLE
            notSeenFeedItemIds.addAll(countNotSeen(items))
            DebugLogUtil.b("Not seen profiles [${getFeedName()}]: ${notSeenFeedItemIds.joinToString(",", "[", "]", transform = { it.substring(0..3) })}")
        }
    }

    // ------------------------------------------
    protected fun markFeedItemAsSeen(feedItemId: String) {
        if (notSeenFeedItemIds.isEmpty()) {
            return
        }

        updateFeedItemAsSeenUseCase.source(params = Params().put("feedItemId", feedItemId).put("isNotSeen", false))
            .autoDisposable(this)
            .subscribe({}, Timber::e)

        if (notSeenFeedItemIds.remove(feedItemId)) {
            DebugLogUtil.b("Seen [${feedItemId.substring(0..3)}]. Left not seen [${getFeedName()}]: ${notSeenFeedItemIds.joinToString(",", "[", "]", transform = { it.substring(0..3) })}")
            if (notSeenFeedItemIds.isEmpty()) {
                DebugLogUtil.b("All seen [${getFeedName()}]")
                runOnUiThread {
                    viewState.value = ViewState.DONE(SEEN_ALL_FEED(getFeedFlag()))
                    viewState.value = ViewState.IDLE
                }
            }
        }
    }

    /* Action Objects */
    // --------------------------------------------------------------------------------------------
    override fun onLike(profileId: String, imageId: String, isLiked: Boolean) {
        super.onLike(profileId, imageId, isLiked)
        addLikedImageForFeedItemIdUseCase.source(params = Params().put("feedItemId", profileId).put("imageId", imageId))
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    fun onFirstUserMessageSent(profileId: String) {
        addUserMessagedFeedItemIdUseCase.source(params = Params().put("feedItemId", profileId))
            .autoDisposable(this)
            .subscribe({}, Timber::e)  // keep those peers that user has sent the first message to
    }

    // --------------------------------------------------------------------------------------------
    override fun onRefresh() {
        super.onRefresh()
        Bus.post(event = BusEvent.RefreshOnLmm)
    }
}
