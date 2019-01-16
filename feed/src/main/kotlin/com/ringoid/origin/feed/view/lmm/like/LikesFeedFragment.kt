package com.ringoid.origin.feed.lmm.like

import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.FeedFragment

class LikesFeedFragment : FeedFragment<LikesFeedViewModel>() {

    override fun getVmClass(): Class<LikesFeedViewModel> = LikesFeedViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_likes_feed
}
