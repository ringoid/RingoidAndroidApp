package com.ringoid.origin.view.feed.lmm.like

import com.ringoid.origin.view.feed.FeedFragment

class LikesFeedFragment : FeedFragment<LikesFeedViewModel>() {

    override fun getVmClass(): Class<LikesFeedViewModel> = LikesFeedViewModel::class.java

    override fun getLayoutId(): Int = 0
}
