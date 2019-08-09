package com.ringoid.origin.view.filters

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.base.viewmodel.LiveEvent
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.domain.model.feed.Filters
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

open class BaseFiltersViewModel @Inject constructor(
    private val filtersSource: IFiltersSource, app: Application) : BaseViewModel(app) {

    val filters by lazy { MutableLiveData<Filters>() }
    protected val filtersChanged = PublishSubject.create<Boolean>()

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
    fun requestFiltersForUpdate() {
        filtersChanged.onNext(true)
    }

    fun setMinMaxAge(minAge: Int, maxAge: Int) {
        val oldFilters = filtersSource.getFilters()
        if (oldFilters.minAge != minAge || oldFilters.maxAge != maxAge) {
            filtersSource.setFilters(Filters(minAge = minAge, maxAge = maxAge, maxDistance = oldFilters.maxDistance))
            requestFiltersForUpdate()
            oneShot.value = LiveEvent(true)
        }
    }

    fun setDistance(distance: Int) {
        val oldFilters = filtersSource.getFilters()
        if (oldFilters.maxDistance != distance) {
            filtersSource.setFilters(Filters(minAge = oldFilters.minAge, maxAge = oldFilters.maxAge, maxDistance = distance))
            requestFiltersForUpdate()
            oneShot.value = LiveEvent(true)
        }
    }
}
