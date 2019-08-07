package com.ringoid.domain.memory

import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.model.feed.Filters

object FiltersInMemoryCache : IFiltersSource {

    private var filters: Filters = Filters()

    override fun getFilters(): Filters = filters

    override fun setFilters(filters: Filters) {
        this.filters = filters
    }

    override fun dropFilters() {
        filters = Filters()
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
