package com.ringoid.origin.view.feed.explore

import com.ringoid.origin.view.feed.FeedFragment

class ExploreFragment : FeedFragment<ExploreViewModel>() {

    companion object {
        fun newInstance(): ExploreFragment = ExploreFragment()
    }

    override fun getVmClass(): Class<ExploreViewModel> = ExploreViewModel::class.java
}
