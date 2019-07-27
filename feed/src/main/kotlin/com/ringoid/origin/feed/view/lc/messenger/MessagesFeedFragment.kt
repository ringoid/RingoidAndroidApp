package com.ringoid.origin.feed.view.lc.messenger

import com.ringoid.origin.feed.view.lc.base.BaseLcFeedFragment

class MessagesFeedFragment : BaseLcFeedFragment<MessagesFeedViewModel>() {

    companion object {
        fun newInstance(): MessagesFeedFragment = MessagesFeedFragment()
    }

    override fun getVmClass(): Class<MessagesFeedViewModel> = MessagesFeedViewModel::class.java
}
