package com.ringoid.origin.feed.view.lc.like

import android.app.Application
import com.ringoid.domain.interactor.feed.GetLcCountersUseCase
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.origin.feed.view.lc.base.LcFeedFiltersViewModel
import com.ringoid.origin.view.main.LcNavTab
import javax.inject.Inject

class LikesFeedFiltersViewModel @Inject constructor(
    getLcUseCountersCase: GetLcCountersUseCase,
    filtersSource: IFiltersSource, app: Application)
    : LcFeedFiltersViewModel(getLcUseCountersCase, filtersSource, app) {

    override fun getFeedName(): String = LcNavTab.LIKES.feedName
}
