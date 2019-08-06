package com.ringoid.origin.view.filters

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.domain.model.essence.feed.FilterEssence
import javax.inject.Inject

class FiltersViewModel @Inject constructor(
    private val filtersSource: IFiltersSource, app: Application)
    : BaseViewModel(app) {

    val filters by lazy { MutableLiveData<FilterEssence>() }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun setUserVisibleHint(isVisibleToUser: Boolean): Boolean {
        if (isVisibleToUser) {
            filtersSource.getFilters()?.let { filters.value = it }
        }
        return super.setUserVisibleHint(isVisibleToUser)
    }

    // --------------------------------------------------------------------------------------------
    fun setMinMaxAge(minAge: Int, maxAge: Int) {
        filtersSource.getFilters()?.let {
            filtersSource.setFilters(FilterEssence.create(minAge = minAge, maxAge = maxAge, maxDistance = it.maxDistance))
        } ?: filtersSource.setFilters(FilterEssence.create(minAge = minAge, maxAge = maxAge))
    }

    fun setDistance(distance: Int) {
        filtersSource.getFilters()?.let {
            filtersSource.setFilters(FilterEssence.create(minAge = it.minAge, maxAge = it.maxAge, maxDistance = distance))
        } ?: filtersSource.setFilters(FilterEssence.create(maxDistance = distance))
    }
}
