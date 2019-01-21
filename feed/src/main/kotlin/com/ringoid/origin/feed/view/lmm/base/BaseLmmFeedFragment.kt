package com.ringoid.origin.feed.view.lmm.base

import android.os.Bundle
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.IFeedViewHolder
import com.ringoid.origin.feed.view.base.BaseFeedFragment

abstract class BaseLmmFeedFragment<VM : BaseLmmFeedViewModel, VH>
    : BaseFeedFragment<VM, FeedItem, VH>() where VH : BaseViewHolder<FeedItem>, VH : IFeedViewHolder {

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewLifecycleOwner.observe(vm.feed, feedAdapter::submitList)
        vm.clearScreen(mode = ViewState.CLEAR.MODE_NEED_REFRESH)
    }
}
