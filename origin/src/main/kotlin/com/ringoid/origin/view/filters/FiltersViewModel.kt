package com.ringoid.origin.view.filters

import android.app.Application
import com.ringoid.domain.memory.IFiltersSource
import javax.inject.Inject

class FiltersViewModel @Inject constructor(filtersSource: IFiltersSource, app: Application)
    : BaseFiltersViewModel(filtersSource, app)
