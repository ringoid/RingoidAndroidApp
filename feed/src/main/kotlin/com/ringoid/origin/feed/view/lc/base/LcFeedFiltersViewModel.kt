package com.ringoid.origin.feed.view.lc.base

import android.app.Application
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.origin.view.filters.BaseFiltersViewModel
import javax.inject.Inject

class LcFeedFiltersViewModel @Inject constructor(filtersSource: IFiltersSource, app: Application)
    : BaseFiltersViewModel(filtersSource, app) {
    //
}
