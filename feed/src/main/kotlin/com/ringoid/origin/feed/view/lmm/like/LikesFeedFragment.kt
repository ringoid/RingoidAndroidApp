package com.ringoid.origin.feed.view.lmm.like

import com.ringoid.base.view.ViewState
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.lmm.like.BaseLikeFeedAdapter
import com.ringoid.origin.feed.adapter.lmm.like.LikeFeedAdapter
import com.ringoid.origin.feed.adapter.lmm.like.LikeFeedViewHolder
import com.ringoid.origin.view.common.EmptyFragment

class LikesFeedFragment : BaseLikesFeedFragment<LikesFeedViewModel, LikeFeedViewHolder>() {

    companion object {
        fun newInstance(): LikesFeedFragment = LikesFeedFragment()
    }

    override fun getVmClass(): Class<LikesFeedViewModel> = LikesFeedViewModel::class.java

    override fun instantiateFeedAdapter(): BaseLikeFeedAdapter<LikeFeedViewHolder> = LikeFeedAdapter()

    override fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input? =
        when (mode) {
            ViewState.CLEAR.MODE_EMPTY_DATA -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_likes_you_empty_no_data)
            ViewState.CLEAR.MODE_NEED_REFRESH -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_likes_you_empty_need_refresh)
            else -> null
        }
}
