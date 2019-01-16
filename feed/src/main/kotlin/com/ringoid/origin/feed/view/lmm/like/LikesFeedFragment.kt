package com.ringoid.origin.feed.view.lmm.like

import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.view.FeedFragment

class LikesFeedFragment : FeedFragment<LikesFeedViewModel>() {

    override fun getVmClass(): Class<LikesFeedViewModel> = LikesFeedViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_likes_feed
}
