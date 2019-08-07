package com.ringoid.origin.feed.view.lc.like

import android.app.Application
import com.ringoid.domain.interactor.feed.GetLcUseCase
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.origin.feed.view.lc.base.LcFeedFiltersViewModel
import com.ringoid.origin.view.main.LcNavTab
import javax.inject.Inject

class LikesFeedFiltersViewModel @Inject constructor(getLcUseCase: GetLcUseCase, filtersSource: IFiltersSource, app: Application)
    : LcFeedFiltersViewModel(getLcUseCase, filtersSource, app) {

    override fun getFeedName(): String = LcNavTab.LIKES.feedName
}
