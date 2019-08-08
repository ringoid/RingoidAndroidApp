package com.ringoid.origin.feed.view.lc.base

import android.app.Application
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.GetLcCountersUseCase
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.origin.utils.ScreenHelper
import com.ringoid.origin.view.filters.BaseFiltersViewModel
import com.ringoid.origin.view.filters.LC_COUNTS
import com.ringoid.utility.inputDebounce
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

abstract class LcFeedFiltersViewModel(
    private val getLcUseCountersCase: GetLcCountersUseCase,
    filtersSource: IFiltersSource, app: Application)
    : BaseFiltersViewModel(filtersSource, app) {

    abstract fun getFeedName(): String

    init {
        // update filters' counts on change LC to reflect any changes, such as blocked or transferred profiles
        getLcUseCountersCase.repository.lmmLoadFinish
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ filtersChanged.onNext(true) }, Timber::e)

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
                viewState.value = ViewState.DONE(LC_COUNTS(countLikes = it.likes.size, countMessages = it.messages.size,
                                                           totalNotFilteredLikes = it.totalNotFilteredLikes,
                                                           totalNotFilteredMessages = it.totalNotFilteredMessages))
            }, Timber::e)
    }
}
