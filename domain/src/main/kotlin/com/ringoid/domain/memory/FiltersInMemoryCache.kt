package com.ringoid.domain.memory

import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.model.essence.feed.FilterEssence

object FiltersInMemoryCache : IFiltersSource {

    private var filters: FilterEssence? = null

    override fun getFilters(): FilterEssence? = filters

    override fun setFilters(filters: FilterEssence) {
        this.filters = filters
    }

    @Synchronized
    fun persist(spm: ISharedPrefsManager) {
        filters?.let { spm.setFilters(it) }
    }

    @Synchronized
    fun restore(spm: ISharedPrefsManager) {
        filters = spm.getFilters()
    }
}
