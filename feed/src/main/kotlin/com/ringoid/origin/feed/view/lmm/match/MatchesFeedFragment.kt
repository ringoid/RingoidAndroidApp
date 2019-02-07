package com.ringoid.origin.feed.view.lmm.match

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.adapter.lmm.BaseLikeFeedAdapter
import com.ringoid.origin.feed.adapter.lmm.match.MatchFeedAdapter
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.origin.feed.view.lmm.like.BaseLikesFeedFragment
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.utility.communicator

class MatchesFeedFragment : BaseLikesFeedFragment<MatchesFeedViewModel, OriginFeedViewHolder<FeedItem>>() {

    companion object {
        fun newInstance(): MatchesFeedFragment = MatchesFeedFragment()
    }

    override fun getVmClass(): Class<MatchesFeedViewModel> = MatchesFeedViewModel::class.java

    override fun instantiateFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool?)
        : BaseLikeFeedAdapter<OriginFeedViewHolder<FeedItem>> =
        MatchFeedAdapter(imagesViewPool).apply {
            onImageToOpenChatClickListener = { model: ProfileImageVO, position: Int ->
                communicator(ILmmFragment::class.java)?.showTabs(isVisible = false)
                openChat(position = position, peerId = model.profileId, imageId = model.image.id)
            }
        }

    override fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input? =
        when (mode) {
            ViewState.CLEAR.MODE_EMPTY_DATA -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_matches_empty_no_data)
            ViewState.CLEAR.MODE_NEED_REFRESH -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_matches_empty_need_refresh)
            else -> null
        }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewLifecycleOwner.observe(vm.badgeMatches) { communicator(ILmmFragment::class.java)?.showBadgeOnMatches(it) }
    }
}
