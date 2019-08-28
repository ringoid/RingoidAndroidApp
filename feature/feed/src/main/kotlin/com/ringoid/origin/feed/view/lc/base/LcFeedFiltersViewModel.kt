package com.ringoid.origin.feed.view.lc.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.viewmodel.LiveEvent
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.GetLcCountersUseCase
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.origin.utils.ScreenHelper
import com.ringoid.origin.view.filters.BaseFiltersViewModel
import com.ringoid.origin.view.filters.FeedFilterCounts
import com.ringoid.utility.inputDebounce
import com.uber.autodispose.lifecycle.autoDisposable

abstract class LcFeedFiltersViewModel(
    private val getLcUseCountersCase: GetLcCountersUseCase,
    filtersSource: IFiltersSource, app: Application)
    : BaseFiltersViewModel(filtersSource, app) {

    private val filterCountsOneShot by lazy { MutableLiveData<LiveEvent<FeedFilterCounts>>() }
    internal fun filterCountsOneShot(): LiveData<LiveEvent<FeedFilterCounts>> = filterCountsOneShot

    abstract fun getFeedName(): String

    init {
        filtersChanged  // performs on main thread
            .compose(inputDebounce())
            .map {
                DebugLogUtil.d("Filters changed on [${getFeedName()}]")
                Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
                        .put("limit", DomainUtil.LIMIT_PER_PAGE)
                        .put("source", getFeedName())
                        .put(filtersSource.getFilters())
            }
            .flatMapSingle { getLcUseCountersCase.source(params = it) }
            .autoDisposable(this)
            .subscribe({
                val counts =
                    FeedFilterCounts(
                        countLikes = it.likes.size,
                        countMessages = it.messages.size,
                        totalNotFilteredLikes = it.totalNotFilteredLikes,
                        totalNotFilteredMessages = it.totalNotFilteredMessages)

                filterCountsOneShot.value = LiveEvent(counts)
            }, DebugLogUtil::e)
    }
}
