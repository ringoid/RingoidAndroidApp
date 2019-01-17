package com.ringoid.origin.feed.view.lmm.match

import com.ringoid.origin.feed.view.lmm.like.BaseLikesFeedFragment

class MatchesFeedFragment : BaseLikesFeedFragment<MatchesFeedViewModel>() {

    override fun getVmClass(): Class<MatchesFeedViewModel> = MatchesFeedViewModel::class.java
}
