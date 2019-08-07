package com.ringoid.origin.feed.view.explore

import android.app.Application
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.origin.view.filters.BaseFiltersViewModel
import javax.inject.Inject

class ExploreFiltersViewModel @Inject constructor(filtersSource: IFiltersSource, app: Application)
    : BaseFiltersViewModel(filtersSource, app) {
    //
}
