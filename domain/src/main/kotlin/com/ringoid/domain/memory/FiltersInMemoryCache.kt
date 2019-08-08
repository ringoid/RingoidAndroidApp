package com.ringoid.domain.memory

import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.model.feed.DefaultFilters
import com.ringoid.domain.model.feed.Filters

object FiltersInMemoryCache : IFiltersSource {

    var isFiltersAppliedOnExplore: Boolean = false
    var isFiltersAppliedOnLc: Boolean = false

    private var filters: Filters = DefaultFilters

    override fun hasFiltersApplied(): Boolean = filters != DefaultFilters

    override fun getFilters(): Filters = filters

    override fun setFilters(filters: Filters) {
        this.filters = filters
    }

    override fun dropFilters() {
        filters = DefaultFilters
    }

    fun clear() {
        dropFilters()
    }

    @Synchronized
    fun persist(spm: ISharedPrefsManager) {
        filters.let { spm.setFilters(it) }
    }

    @Synchronized
    fun restore(spm: ISharedPrefsManager) {
        filters = spm.getFilters()
    }
}
