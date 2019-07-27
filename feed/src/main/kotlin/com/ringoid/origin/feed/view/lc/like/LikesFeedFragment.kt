package com.ringoid.origin.feed.view.lc.like

import com.ringoid.origin.feed.view.lc.base.BaseLcFeedFragment

class LikesFeedFragment : BaseLcFeedFragment<LikesFeedViewModel>() {

    companion object {
        fun newInstance(): LikesFeedFragment = LikesFeedFragment()
    }

    override fun getVmClass(): Class<LikesFeedViewModel> = LikesFeedViewModel::class.java
}
