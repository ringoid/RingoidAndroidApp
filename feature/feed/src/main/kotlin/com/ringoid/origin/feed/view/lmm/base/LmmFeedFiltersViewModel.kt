package com.ringoid.origin.feed.view.lmm.base

import android.app.Application
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.origin.view.filters.BaseFiltersViewModel
import javax.inject.Inject

@Deprecated("LMM -> LC")  // compatibility class
class LmmFeedFiltersViewModel @Inject constructor(filtersSource: IFiltersSource, app: Application)
    : BaseFiltersViewModel(filtersSource, app)
