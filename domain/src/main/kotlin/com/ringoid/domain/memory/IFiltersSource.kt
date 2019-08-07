package com.ringoid.domain.memory

import com.ringoid.domain.model.feed.Filters

interface IFiltersSource  {

    fun getFilters(): Filters

    fun setFilters(filters: Filters)

    fun dropFilters()
}
