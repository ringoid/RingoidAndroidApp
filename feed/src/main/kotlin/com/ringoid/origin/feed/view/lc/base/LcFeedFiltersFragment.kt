package com.ringoid.origin.feed.view.lc.base

import com.ringoid.origin.view.filters.BaseFiltersFragment

class LcFeedFiltersFragment : BaseFiltersFragment<LcFeedFiltersViewModel>() {

    companion object {
        fun newInstance(): LcFeedFiltersFragment = LcFeedFiltersFragment()
    }

    override fun getVmClass(): Class<LcFeedFiltersViewModel> = LcFeedFiltersViewModel::class.java
}
