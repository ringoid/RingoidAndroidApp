package com.ringoid.origin.feed.view.explore

import com.ringoid.origin.view.filters.BaseFiltersFragment

class ExploreFiltersFragment : BaseFiltersFragment<ExploreFiltersViewModel>() {

    companion object {
        fun newInstance(): ExploreFiltersFragment = ExploreFiltersFragment()
    }

    override fun getVmClass(): Class<ExploreFiltersViewModel> = ExploreFiltersViewModel::class.java
}
