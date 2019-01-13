package com.ringoid.origin.feed.lmm.message

import com.ringoid.origin.feed.FeedFragment
import com.ringoid.origin.feed.R

class MessagesFeedFragment : FeedFragment<MessagesFeedViewModel>() {

    override fun getVmClass(): Class<MessagesFeedViewModel> = MessagesFeedViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_messages_feed
}
