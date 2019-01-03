package com.ringoid.origin.view.feed.lmm.match

import com.ringoid.origin.view.feed.FeedFragment

class MatchesFeedFragment : FeedFragment<MatchesFeedViewModel>() {

    override fun getVmClass(): Class<MatchesFeedViewModel> = MatchesFeedViewModel::class.java

    override fun getLayoutId(): Int = 0
}
