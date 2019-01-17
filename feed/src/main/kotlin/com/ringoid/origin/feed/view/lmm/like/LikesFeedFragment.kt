package com.ringoid.origin.feed.view.lmm.like

class LikesFeedFragment : BaseLikesFeedFragment<LikesFeedViewModel>() {

    companion object {
        fun newInstance(): LikesFeedFragment = LikesFeedFragment()
    }

    override fun getVmClass(): Class<LikesFeedViewModel> = LikesFeedViewModel::class.java
}
