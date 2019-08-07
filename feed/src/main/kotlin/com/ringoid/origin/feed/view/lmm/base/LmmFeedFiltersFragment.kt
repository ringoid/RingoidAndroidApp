package com.ringoid.origin.feed.view.lmm.base

import com.ringoid.origin.view.filters.BaseFiltersFragment

@Deprecated("LMM -> LC")  // compatibility class
class LmmFeedFiltersFragment : BaseFiltersFragment<LmmFeedFiltersViewModel>() {

    companion object {
        fun newInstance(): LmmFeedFiltersFragment = LmmFeedFiltersFragment()
    }

    override fun getVmClass(): Class<LmmFeedFiltersViewModel> = LmmFeedFiltersViewModel::class.java
}
