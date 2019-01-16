package com.ringoid.origin.feed.view.lmm.match

import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.view.FeedFragment

class MatchesFeedFragment : FeedFragment<MatchesFeedViewModel>() {

    override fun getVmClass(): Class<MatchesFeedViewModel> = MatchesFeedViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_matches_feed
}
