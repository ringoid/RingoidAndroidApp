package com.ringoid.domain.memory

import com.ringoid.domain.model.essence.feed.FilterEssence

interface IFiltersSource  {

    fun getFilters(): FilterEssence?

    fun setFilters(filters: FilterEssence)

    fun dropFilters()
}
