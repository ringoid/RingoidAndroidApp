package com.ringoid.origin.feed.view.explore

import com.ringoid.origin.view.filters.BaseFiltersFragment

class ExploreFeedFiltersFragment : BaseFiltersFragment<ExploreFeedFiltersViewModel>() {

    companion object {
        fun newInstance(): ExploreFeedFiltersFragment = ExploreFeedFiltersFragment()
    }

    override fun getVmClass(): Class<ExploreFeedFiltersViewModel> = ExploreFeedFiltersViewModel::class.java
}
