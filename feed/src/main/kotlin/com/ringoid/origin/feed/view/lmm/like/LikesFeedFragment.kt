package com.ringoid.origin.feed.view.lmm.like

import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.FeedAdapter
import com.ringoid.origin.feed.adapter.lmm.like.LikeFeedAdapter
import com.ringoid.origin.feed.view.FeedFragment

class LikesFeedFragment : FeedFragment<LikesFeedViewModel>() {

    override fun getVmClass(): Class<LikesFeedViewModel> = LikesFeedViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_feed

    override fun createFeedAdapter(): FeedAdapter =
        LikeFeedAdapter().apply {
            messageClickListener = { model: Profile, _ ->
                // TODO: open chat
            }
        }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
}
