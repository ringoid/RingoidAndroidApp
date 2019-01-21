package com.ringoid.origin.feed.view.lmm.messenger

import com.ringoid.base.view.ViewState
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.adapter.lmm.messenger.MessengerFeedAdapter
import com.ringoid.origin.feed.adapter.lmm.messenger.MessengerViewHolder
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedFragment
import com.ringoid.origin.view.common.EmptyFragment

class MessengerFragment : BaseLmmFeedFragment<MessengerViewModel, MessengerViewHolder>() {

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
}
