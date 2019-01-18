package com.ringoid.origin.feed.view.lmm.like

import android.os.Bundle
import android.view.View
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.lmm.like.LikeFeedAdapter
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.utility.communicator
import kotlinx.android.synthetic.main.fragment_feed.*

class LikesFeedFragment : BaseLikesFeedFragment<LikesFeedViewModel>() {

    companion object {
        fun newInstance(): LikesFeedFragment = LikesFeedFragment()
    }

    override fun getVmClass(): Class<LikesFeedViewModel> = LikesFeedViewModel::class.java

    override fun instantiateFeedAdapter(): LikeFeedAdapter = LikeFeedAdapter()

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
        communicator(ILmmFragment::class.java)
            ?.getViewModel()
            ?.apply {
                viewLifecycleOwner.apply {
                    observe(viewState, this@LikesFeedFragment::onViewStateChange)
                    observe(emptyStateLikes, this@LikesFeedFragment::onViewStateChange)
                    observe(feedLikes) { feedAdapter.submitList(it.map { it.profile() }) }
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipe_refresh_layout.apply {
            setOnRefreshListener { communicator(ILmmFragment::class.java)?.onRefresh() }
        }
    }
}
