package com.ringoid.origin.feed.view.lmm.like

import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.feed.adapter.lmm.like.LikeFeedAdapter
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.feed.view.FeedViewModel

abstract class BaseLikesFeedFragment<T : FeedViewModel> : FeedFragment<T>() {

    abstract fun instantiateFeedAdapter(): LikeFeedAdapter

    override fun createFeedAdapter(): LikeFeedAdapter =
        instantiateFeedAdapter().apply {
            messageClickListener = { model: Profile, _ ->
                // TODO: open chat
            }
        }
}
