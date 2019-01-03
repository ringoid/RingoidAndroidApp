package com.ringoid.origin.view.feed.lmm.message

import com.ringoid.origin.view.feed.FeedFragment

class MessagesFeedFragment : FeedFragment<MessagesFeedViewModel>() {

    override fun getVmClass(): Class<MessagesFeedViewModel> = MessagesFeedViewModel::class.java

    override fun getLayoutId(): Int = 0
}
