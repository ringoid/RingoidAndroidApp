package com.ringoid.origin.feed.lmm.match

import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.FeedFragment

class MatchesFeedFragment : FeedFragment<MatchesFeedViewModel>() {

    override fun getVmClass(): Class<MatchesFeedViewModel> = MatchesFeedViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_matches_feed
}
