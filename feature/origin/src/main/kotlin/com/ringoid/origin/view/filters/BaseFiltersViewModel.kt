package com.ringoid.origin.view.filters

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.base.viewmodel.OneShot
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.domain.model.feed.Filters
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

open class BaseFiltersViewModel @Inject constructor(
    private val filtersSource: IFiltersSource, app: Application) : BaseViewModel(app) {

    private val filters by lazy { MutableLiveData<Filters>() }
    private val filtersChangeOneShot by lazy { MutableLiveData<OneShot<Boolean>>() }
    internal fun filters(): LiveData<Filters> = filters
    internal fun filtersChangeOneShot(): LiveData<OneShot<Boolean>> = filtersChangeOneShot

    private val filtersUpdateRequest = PublishSubject.create<Boolean>()
    protected fun filtersUpdateRequestSource(): Observable<Boolean> = filtersUpdateRequest.hide()

    private fun setUpFilters() {
        val filtersValue = filtersSource.getFilters()
        Timber.i("Filters: $filtersValue")
        filters.value = filtersValue
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun handleVisibleHintChange(isVisibleToUser: Boolean) {
        super.handleVisibleHintChange(isVisibleToUser)
        if (isVisibleToUser) {
            setUpFilters()
        }
    }

    // --------------------------------------------------------------------------------------------
    internal fun requestFiltersForUpdate() {
        filtersUpdateRequest.onNext(true)
    }

    internal fun setMinMaxAge(minAge: Int, maxAge: Int) {
        val oldFilters = filtersSource.getFilters()
        if (oldFilters.minAge != minAge || oldFilters.maxAge != maxAge) {
            filtersSource.setFilters(Filters.create(minAge = minAge, maxAge = maxAge, maxDistance = oldFilters.maxDistance))
            requestFiltersForUpdate()
            filtersChangeOneShot.value = OneShot(true)
        }
    }

    internal fun setDistance(distance: Int) {
        val oldFilters = filtersSource.getFilters()
        if (oldFilters.maxDistance != distance) {
            filtersSource.setFilters(Filters.create(minAge = oldFilters.minAge, maxAge = oldFilters.maxAge, maxDistance = distance))
            requestFiltersForUpdate()
            filtersChangeOneShot.value = OneShot(true)
        }
    }
}
