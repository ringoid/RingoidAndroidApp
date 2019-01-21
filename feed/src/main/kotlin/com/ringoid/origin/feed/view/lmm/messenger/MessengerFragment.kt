package com.ringoid.origin.feed.view.lmm.messenger

import android.os.Bundle
import android.view.View
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.adapter.lmm.messenger.MessengerFeedAdapter
import com.ringoid.origin.feed.adapter.lmm.messenger.MessengerViewHolder
import com.ringoid.origin.feed.view.base.BaseFeedFragment
import com.ringoid.origin.view.common.EmptyFragment
import kotlinx.android.synthetic.main.fragment_feed.*

class MessengerFragment : BaseFeedFragment<MessengerViewModel, FeedItem, MessengerViewHolder>() {

    companion object {
        fun newInstance(): MessengerFragment = MessengerFragment()
    }

    override fun getVmClass(): Class<MessengerViewModel> = MessengerViewModel::class.java

    override fun createFeedAdapter(): BaseFeedAdapter<FeedItem, MessengerViewHolder> = MessengerFeedAdapter()

    override fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input? =
        when (mode) {
            ViewState.CLEAR.MODE_EMPTY_DATA -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_messages_empty_no_data)
            ViewState.CLEAR.MODE_NEED_REFRESH -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_messages_empty_need_refresh)
            else -> null
        }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewLifecycleOwner.observe(vm.feed, feedAdapter::submitList)
        vm.clearScreen(mode = ViewState.CLEAR.MODE_NEED_REFRESH)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipe_refresh_layout.apply {
//            setColorSchemeResources(*resources.getIntArray(R.array.swipe_refresh_colors))
            setOnRefreshListener { vm.onRefresh() }
        }
    }
}
