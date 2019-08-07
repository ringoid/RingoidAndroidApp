package com.ringoid.origin.view.filters

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.domain.model.feed.Filters
import timber.log.Timber
import javax.inject.Inject

open class BaseFiltersViewModel @Inject constructor(
    private val filtersSource: IFiltersSource, app: Application) : BaseViewModel(app) {

    val filters by lazy { MutableLiveData<Filters>() }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun setUserVisibleHint(isVisibleToUser: Boolean): Boolean {
        if (isVisibleToUser) {
            Timber.i("Filters: ${filtersSource.getFilters()}")
            filters.value = filtersSource.getFilters()
        }
        return super.setUserVisibleHint(isVisibleToUser)
    }

    override fun onStart() {
        super.onStart()
        setUserVisibleHint(isVisibleToUser = true)  // initialize filters
    }

    override fun onStop() {
        super.onStop()
        setUserVisibleHint(isVisibleToUser = false)
    }

    // --------------------------------------------------------------------------------------------
    fun setMinMaxAge(minAge: Int, maxAge: Int) {
        filtersSource.getFilters().let {
            filtersSource.setFilters(Filters(minAge = minAge, maxAge = maxAge, maxDistance = it.maxDistance))
        }
    }

    fun setDistance(distance: Int) {
        filtersSource.getFilters().let {
            filtersSource.setFilters(Filters(minAge = it.minAge, maxAge = it.maxAge, maxDistance = distance))
        }
    }
}
