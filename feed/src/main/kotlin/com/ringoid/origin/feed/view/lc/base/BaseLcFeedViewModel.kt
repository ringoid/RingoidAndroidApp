package com.ringoid.origin.feed.view.lc.base

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLcUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesForChatUseCase
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.essence.feed.FilterEssence
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.view.FeedViewModel
import com.ringoid.origin.utils.ScreenHelper
import com.ringoid.origin.view.main.LmmNavTab
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

abstract class BaseLcFeedViewModel(
    protected val getLcUseCase: GetLcUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    clearMessagesForChatUseCase: ClearMessagesForChatUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
    userInMemoryCache: IUserInMemoryCache, app: Application)
    : FeedViewModel(
        clearCachedAlreadySeenProfileIdsUseCase,
        clearMessagesForChatUseCase,
        cacheBlockedProfileIdUseCase,
        countUserImagesUseCase,
        userInMemoryCache, app) {

    val count by lazy { MutableLiveData<Int>() }
    val feed by lazy { MutableLiveData<List<FeedItemVO>>() }

    protected var badgeIsOn: Boolean = false  // indicates that there are new feed items
        private set

    protected abstract fun getSourceFeed(): LmmNavTab

    init {
        sourceBadge()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeIsOn = it }, Timber::e)

        sourceFeed()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { setLcItems(items = it, clearMode = ViewState.CLEAR.MODE_EMPTY_DATA) }
            // TODO: analyze for the first reply in messages only once per user session
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    // --------------------------------------------------------------------------------------------
    protected abstract fun getFeedFlag(): Int
    protected abstract fun getFeedFromLmm(lmm: Lmm): List<FeedItem>
    protected abstract fun sourceBadge(): Observable<Boolean>
    protected abstract fun sourceFeed(): Observable<List<FeedItem>>

    override fun getFeed() {
        val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
                             .put("limit", DomainUtil.LIMIT_PER_PAGE)
                             .put(FilterEssence.create())  // TODO: prepare global filters
                             .put("source", getFeedName())

        getLcUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    private fun setLcItems(items: List<FeedItem>, clearMode: Int = ViewState.CLEAR.MODE_NEED_REFRESH) {
        // TODO: clear discardedFeedItemIds
        // TODO: clear notSeenFeedItemIds

        count.value = items.size
        if (items.isEmpty()) {
            feed.value = emptyList()
            viewState.value = ViewState.CLEAR(mode = clearMode)
        } else {
            feed.value = items.map { FeedItemVO(it) }
            viewState.value = ViewState.IDLE
            // TODO: countNotSeen
        }
    }
}
