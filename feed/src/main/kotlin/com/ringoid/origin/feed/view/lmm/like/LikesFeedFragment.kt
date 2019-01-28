package com.ringoid.origin.feed.view.lmm.like

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.adapter.lmm.like.BaseLikeFeedAdapter
import com.ringoid.origin.feed.adapter.lmm.like.LikeFeedAdapter
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.utility.communicator

class LikesFeedFragment : BaseLikesFeedFragment<LikesFeedViewModel, OriginFeedViewHolder<FeedItem>>() {

    companion object {
        fun newInstance(): LikesFeedFragment = LikesFeedFragment()
    }

    override fun getVmClass(): Class<LikesFeedViewModel> = LikesFeedViewModel::class.java

    override fun instantiateFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool?)
        : BaseLikeFeedAdapter<OriginFeedViewHolder<FeedItem>> = LikeFeedAdapter(imagesViewPool)

    override fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input? =
        when (mode) {
            ViewState.CLEAR.MODE_EMPTY_DATA -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_likes_you_empty_no_data)
            ViewState.CLEAR.MODE_NEED_REFRESH -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_likes_you_empty_need_refresh)
            else -> null
        }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewLifecycleOwner.observe(vm.badgeLikes) { communicator(ILmmFragment::class.java)?.showBadgeOnLikes(it) }
    }
}
